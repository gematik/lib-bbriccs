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

package de.gematik.bbriccs.rest.vzd.auth;

import de.gematik.bbriccs.idp.data.Nonce;
import de.gematik.bbriccs.idp.exception.AuthException;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBRequestCustomizer;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.idp.client.AuthorizationCodeResult;
import de.gematik.idp.client.IdpClient;
import de.gematik.idp.crypto.EcSignerUtility;
import de.gematik.idp.crypto.RsaSignerUtility;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import tools.jackson.databind.ObjectMapper;

/**
 * Retrieves and signs the challenge for the owner authentication flow in gemSpec section 5.2.
 * Encryption and completion of the authentication flow are not implemented yet.
 */
@Slf4j
public class OwnerAccessTokenHeaderProvider implements HttpBRequestCustomizer {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final URI authServiceUri;
  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
  private final Smartcard smartcard;

  private VzdAccessToken accessToken;
  private Instant accessTokenValidUntil =
      Instant.now(); // point in time when the IDP token needs to be updated

  private OwnerAccessTokenHeaderProvider(URI authServiceUri, Smartcard smartcard) {
    this.authServiceUri = authServiceUri;
    this.smartcard = smartcard;
  }

  @Override
  public List<HttpHeader> forRequest(HttpBRequest request) {
    this.refreshAccessToken();
    return List.of(JwtHeaderKey.AUTHORIZATION.createHeader(this.accessToken.accessToken()));
  }

  private void refreshAccessToken() {
    val refreshInstant = accessTokenValidUntil.minus(120, ChronoUnit.SECONDS);

    if (Instant.now().isAfter(refreshInstant)) {
      log.info("Refreshing VZD access token for owner authentication flow");
      val redirect = initiateOwnerAuthenticate();
      val authCode = challengeGematikIdp(redirect);
      this.accessToken = exchangeForVzdAccessToken(authCode);

      // store the time when the access token needs to be refreshed
      accessTokenValidUntil = Instant.now().plus(this.accessToken.expiresIn(), ChronoUnit.SECONDS);
    } else {
      log.debug("VZD access token is still valid, no refresh needed");
    }
  }

  @SneakyThrows
  private FhirVzdIdpRedirect initiateOwnerAuthenticate() {
    val ownerAuthUri = authServiceUri.resolve("/owner-authenticate");

    val resp =
        httpClient.send(
            HttpRequest.newBuilder(ownerAuthUri).GET().build(),
            HttpResponse.BodyHandlers.ofString());

    val location =
        resp.headers()
            .firstValue("Location")
            .orElseThrow(
                () ->
                    new AuthException(
                        "No Location header in response for GET /owner-authenticate"));
    return FhirVzdIdpRedirect.from(location);
  }

  private AuthorizationCodeResult challengeGematikIdp(FhirVzdIdpRedirect redirect) {
    val aut = smartcard.getAutCertificate();
    val autCert = aut.getX509Certificate();
    val privateKey = aut.getPrivateKey();

    UnaryOperator<byte[]> contentSigner =
        data ->
            privateKey instanceof RSAPrivateKey
                ? RsaSignerUtility.createRsaSignature(data, privateKey)
                : EcSignerUtility.createEcSignature(data, privateKey);
    val challenge = redirect.getCodeChallenge().value();
    val state = redirect.getState().value();
    val nonce = Nonce.random().value();

    val idp = initIdpClientFor(redirect);
    return idp.login(autCert, contentSigner, challenge, state, nonce);
  }

  @SneakyThrows
  private VzdAccessToken exchangeForVzdAccessToken(AuthorizationCodeResult authCode) {
    val code = URLEncoder.encode(authCode.getAuthorizationCode(), StandardCharsets.UTF_8);
    val state2 = URLEncoder.encode(authCode.getState(), StandardCharsets.UTF_8);
    val uri = URI.create(authCode.getRedirectUri() + "?code=" + code + "&state=" + state2);
    val resp =
        httpClient.send(
            HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());

    return objectMapper.readValue(resp.body(), VzdAccessToken.class);
  }

  private IdpClient initIdpClientFor(FhirVzdIdpRedirect redirect) {
    val dd = redirect.getBaseUrl() + "/.well-known/openid-configuration";
    val idpClient =
        IdpClient.builder()
            .clientId(redirect.getClientId().value())
            .redirectUrl(redirect.getRedirectUri().value())
            .discoveryDocumentUrl(dd)
            .scopes(redirect.getScopesSet())
            .build();
    idpClient.initialize();
    return idpClient;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private URI authServiceUri;
    private Smartcard smartcard;

    private Builder() {}

    /**
     * This is the URL of the auth-service endpoint that is used to perform the owner authentication
     * flow. It must be an absolute URL.
     *
     * @param authServiceUrl the URL of the auth-service endpoint on the VZD that is used to perform
     *     the owner authentication flow
     * @return this builder
     */
    public Builder withAuthService(String authServiceUrl) {
      this.authServiceUri =
          URI.create(
              Objects.requireNonNull(authServiceUrl, "Owner-authenticate URL must be provided"));
      if (!authServiceUri.isAbsolute() || authServiceUri.getHost() == null) {
        throw new IllegalArgumentException("Owner-authenticate URL must be an absolute URL");
      }
      return this;
    }

    public Builder withSmartcard(Smartcard smartcard) {
      this.smartcard = smartcard;
      return this;
    }

    public OwnerAccessTokenHeaderProvider build() {

      return new OwnerAccessTokenHeaderProvider(authServiceUri, smartcard);
    }
  }
}
