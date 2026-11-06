/*
 * Copyright (Change Date see Readme) gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.bbriccs.gid;

import de.gematik.bbriccs.gid.config.GidAuthenticationConfig;
import de.gematik.bbriccs.gid.response.GidAccessTokenResult;
import de.gematik.bbriccs.idp.data.AuthCode;
import de.gematik.bbriccs.idp.data.AuthRequestRedirect;
import de.gematik.bbriccs.idp.data.AuthResponseRedirect;
import de.gematik.bbriccs.idp.data.CodeChallengeMethod;
import de.gematik.bbriccs.idp.data.FormUrlEncoding;
import de.gematik.bbriccs.idp.data.Nonce;
import de.gematik.bbriccs.idp.data.ResponseType;
import de.gematik.bbriccs.idp.data.State;
import de.gematik.bbriccs.idp.exception.AuthException;
import de.gematik.bbriccs.jwt.JweTokenParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
class GidClientImpl implements GidClient {

  private final GidAuthenticationConfig config;
  private final JweTokenParser jweTokenParser = new JweTokenParser();
  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  /**
   * {@inheritDoc}
   *
   * <p>Runs the complete Gesundheits-ID login as four consecutive OAuth2 steps:
   *
   * <ol>
   *   <li>{@link #requestFederationAuthorizationRedirect authorization request} to the Fachdienst
   *   <li>{@link #requestSectoralIdpAuthorization user authentication} at the sektorale IdP
   *   <li>{@link #forwardSectoralIdpAuthorizationResponse delivery} of the IdP response to the
   *       Fachdienst
   *   <li>{@link #exchangeAuthorizationCodeForAccessToken token exchange} and JWE decryption
   * </ol>
   *
   * Redirects are never followed automatically; each {@code Location} header is evaluated
   * explicitly so the intermediate authorization codes can be extracted.
   */
  @Override
  public GidAccessTokenResult login() {
    val kvnr = config.getKvnr();
    val authorizationRequestState = State.from(UUID.randomUUID().toString());
    val nonce = Nonce.random();

    val federationAuthorizationRedirect =
        requestFederationAuthorizationRedirect(authorizationRequestState, nonce);
    val sectoralIdpAuthorizationRedirect =
        requestSectoralIdpAuthorization(federationAuthorizationRedirect, kvnr);
    val authorizationCode =
        forwardSectoralIdpAuthorizationResponse(sectoralIdpAuthorizationRedirect);

    return exchangeAuthorizationCodeForAccessToken(authorizationCode);
  }

  /**
   * Step 1: Sends the OAuth2/PKCE authorization request to the federation authorization endpoint
   *
   * <p>The Fachdienst does not authenticate the user itself; it answers with a 302 redirect to the
   * authorization endpoint of the configured sektorale IdP, carrying a pushed {@code request_uri}
   * that references the authorization request.
   *
   * @param state opaque state bound to this authorization request
   * @param nonce nonce bound to the resulting ID token
   * @return the redirect to the sektorale IdP including the {@code request_uri}
   */
  @SneakyThrows
  private AuthRequestRedirect requestFederationAuthorizationRedirect(State state, Nonce nonce) {
    val fedAuthEndpoint = config.getFedAuthEndpoint();
    val authorizationRequestParameters = buildAuthorizationRequestParameters(state, nonce);

    val authorizationRequestUrl =
        FormUrlEncoding.appendQuery(fedAuthEndpoint, authorizationRequestParameters);

    val authorizationRequestResponse =
        httpClient.send(
            HttpRequest.newBuilder(authorizationRequestUrl).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    return AuthRequestRedirect.from(
        locationHeaderOf(authorizationRequestResponse, "authorization request"));
  }

  /**
   * Step 2: Authenticates the user at the sektorale IdP referenced by the federation redirect.
   *
   * <p>Follows the redirect from step 1 and presents the pushed {@code request_uri} together with
   * the KVNR of the eGK as {@code user_id}. On success the sektorale IdP issues an authorization
   * code and redirects back to the Fachdienst with {@code code} and {@code state}.
   *
   * @param federationAuthorizationRedirect redirect returned by the federation authorization step
   * @param kvnr health insurance number identifying the user at the sektorale IdP
   * @return the authorization response redirect containing the sektorale IdP authorization code
   */
  @SneakyThrows
  private AuthResponseRedirect requestSectoralIdpAuthorization(
      AuthRequestRedirect federationAuthorizationRedirect, String kvnr) {

    val authorizationUrl = federationAuthorizationRedirect.getAuthorizationUriFor(kvnr);
    val sectoralIdpAuthorizationResponse =
        httpClient.send(
            HttpRequest.newBuilder(authorizationUrl).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    return AuthResponseRedirect.from(
        locationHeaderOf(sectoralIdpAuthorizationResponse, "sectoral IdP authorization"));
  }

  /**
   * Step 3: Delivers the sektorale IdP authorization response to the Fachdienst.
   *
   * <p>Posts the sektorale IdP {@code code} and {@code state} to the federation authorization
   * endpoint. The Fachdienst redeems that code with the sektorale IdP on its own and, in turn,
   * issues its own authorization code for this client, returned as a redirect to the registered
   * {@code redirect_uri}.
   *
   * @param sectoralIdpAuthorizationRedirect authorization response redirect from the sektorale IdP
   * @return the redirect carrying the Fachdienst authorization code
   */
  @SneakyThrows
  private AuthCode forwardSectoralIdpAuthorizationResponse(
      AuthResponseRedirect sectoralIdpAuthorizationRedirect) {

    val formBody = sectoralIdpAuthorizationRedirect.asFormBody();
    val federationCodeExchangeResponse =
        httpClient.send(
            HttpRequest.newBuilder(URI.create(config.getFedAuthEndpoint()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    return AuthCode.from(
        locationHeaderOf(federationCodeExchangeResponse, "federation code exchange"));
  }

  /**
   * Step 4: Redeems the Fachdienst authorization code at the token endpoint.
   *
   * <p>Performs the {@code authorization_code} grant including the PKCE verifier. The returned
   * {@code access_token} is a compact JWE which is decrypted with the token key; the decrypted
   * payload contains the actual access token as nested JWT ({@code njwt}).
   *
   * @param authorizationCode authorization code issued by the Fachdienst in step 3
   * @return the encrypted access token, its decrypted payload, and the nested JWT
   */
  @SneakyThrows
  private GidAccessTokenResult exchangeAuthorizationCodeForAccessToken(AuthCode authorizationCode) {

    val clientId = config.getClientId();
    val redirectUri = config.getRedirectUri();
    val keyVerifier = config.getKeyVerifier();
    val tokenRequestBody = authorizationCode.asFormBody(clientId, redirectUri, keyVerifier);

    val tokenResponse =
        httpClient.send(
            HttpRequest.newBuilder(URI.create(config.getTokenEndpoint()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                .build(),
            HttpResponse.BodyHandlers.ofString());

    val encryptedAccessToken = jweTokenParser.extractAccessToken(tokenResponse.body());
    val decryptedPayload = jweTokenParser.decrypt(encryptedAccessToken, config.getTokenKey());
    val nestedJwt = jweTokenParser.extractNestedJwt(decryptedPayload);

    return GidAccessTokenResult.builder()
        .encryptedAccessToken(encryptedAccessToken)
        .decryptedPayload(decryptedPayload)
        .nestedJwt(nestedJwt)
        .build();
  }

  /**
   * Assembles the OAuth2 authorization request parameters (client, PKCE challenge, state, nonce,
   * scope) plus the {@code idp_iss} selecting the sektorale IdP; insertion order is preserved.
   */
  private Map<String, String> buildAuthorizationRequestParameters(State state, Nonce nonce) {
    val params = new LinkedHashMap<String, String>();
    params.put("state", state.value());
    params.put("nonce", nonce.value());
    params.put("client_id", config.getClientId().value());
    params.put("redirect_uri", config.getRedirectUri().value());
    params.put("code_challenge", config.getCodeChallenge().value());
    params.put("code_challenge_method", CodeChallengeMethod.S256.getValue());
    params.put("response_type", ResponseType.CODE.value());
    params.put("scope", config.getScopeValue());
    params.put("idp_iss", config.getSekIdpIss());
    return params;
  }

  /**
   * Returns the {@code Location} header of a redirect response.
   *
   * @throws AuthException if the response carries no {@code Location} header
   */
  private String locationHeaderOf(HttpResponse<String> response, String flowOperation) {
    return response
        .headers()
        .firstValue("Location")
        .orElseThrow(
            () ->
                new AuthException(
                    "Missing Location header in redirect response after " + flowOperation));
  }
}
