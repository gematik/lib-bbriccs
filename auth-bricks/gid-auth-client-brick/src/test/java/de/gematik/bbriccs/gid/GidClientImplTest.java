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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.gematik.bbriccs.gid.config.GidAuthenticationConfig;
import de.gematik.bbriccs.gid.response.GidAccessTokenResult;
import de.gematik.bbriccs.idp.data.Scope;
import de.gematik.bbriccs.idp.exception.AuthException;
import de.gematik.bbriccs.jwt.JweTokenParser;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class GidClientImplTest {

  /**
   * WireMock server with verbose request/response logging. The {@code WireMock} logger is routed to
   * a dedicated traffic log file in {@code logback-test.xml}, so the OAuth2 flow can be followed
   * hop by hop.
   */
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
          .configureStaticDsl(true)
          .build();

  /**
   * Specifies the successful eGK login flow without depending on a live IdP: each redirect and
   * endpoint exchange is served by WireMock, and the returned token must be decrypted into the
   * expected payload and nested JWT. The request assertions also ensure the client carries the
   * identity, client, scope, and authorization-code data through the flow.
   */
  @Test
  void shouldLoginSuccessfullyUsingMockedIdpEndpoints() {
    val baseUrl = wireMock.baseUrl();
    val fedAuthEndpoint = baseUrl + "/fedauth";
    val tokenEndpoint = baseUrl + "/token";
    val payload = "{\"njwt\":\"header.payload.signature\"}";
    val tokenKey = "0123456789abcdef0123456789abcdef".getBytes();
    val encryptedAccessToken = new JweTokenParser().encrypt(payload, tokenKey);

    stubFor(
        get(urlPathEqualTo("/fedauth"))
            .willReturn(
                aResponse()
                    .withStatus(302)
                    .withHeader(
                        "Location",
                        baseUrl
                            + "/gsi-auth?request_uri=mock-request-uri&client_id=myTestApp&state=ma-test-state")));
    stubFor(
        get(urlPathEqualTo("/gsi-auth"))
            .willReturn(
                aResponse()
                    .withStatus(302)
                    .withHeader(
                        "Location", baseUrl + "/callback?code=gsi-code&state=fachdienst-state")));
    stubFor(
        post(urlPathEqualTo("/fedauth"))
            .willReturn(
                aResponse()
                    .withStatus(302)
                    .withHeader(
                        "Location",
                        baseUrl + "/callback?code=fachdienst-code&state=fachdienst-state")));
    stubFor(
        post(urlPathEqualTo("/token"))
            .willReturn(okJson("{\"access_token\":\"" + encryptedAccessToken + "\"}")));

    val client =
        GidClient.create(
                GidAuthenticationConfig.builder()
                    .fedAuthEndpoint(fedAuthEndpoint)
                    .tokenEndpoint(tokenEndpoint)
                    .sekIdpIss("https://example.org/idp")
                    .clientId("eRezeptApp")
                    .redirectUri("https://redirect.example.org/callback")
                    .codeChallenge("test-code-challenge")
                    .codeVerifier("test-code-verifier")
                    .keyVerifier("test-key-verifier")
                    .tokenKey(tokenKey)
                    .scope(Scope.openId())
                    .kvnr("X110411675")
                    .build())
            .initialize();

    GidAccessTokenResult result = client.login();

    assertEquals(encryptedAccessToken, result.getEncryptedAccessToken());
    assertEquals(payload, result.getDecryptedPayload());
    assertEquals("header.payload.signature", result.getNestedJwt());

    verify(
        getRequestedFor(urlPathEqualTo("/fedauth"))
            .withQueryParam("client_id", equalTo("eRezeptApp"))
            .withQueryParam("scope", equalTo("openid"))
            .withQueryParam("idp_iss", equalTo("https://example.org/idp")));
    verify(
        getRequestedFor(urlPathEqualTo("/gsi-auth"))
            .withQueryParam("request_uri", equalTo("mock-request-uri"))
            .withQueryParam("user_id", equalTo("X110411675")));
    verify(
        postRequestedFor(urlPathEqualTo("/fedauth"))
            .withRequestBody(containing("code=gsi-code"))
            .withRequestBody(containing("state=fachdienst-state")));
    verify(
        postRequestedFor(urlPathEqualTo("/token"))
            .withRequestBody(containing("code=fachdienst-code"))
            .withRequestBody(containing("client_id=eRezeptApp")));
  }

  /**
   * The federated IdP rejects the authorization request with a 400 and no {@code Location} header.
   * The client must not continue the flow but fail fast with an {@link AuthException}.
   */
  @Test
  void shouldThrowAuthExceptionWhenFedAuthRejectsWithoutLocation() {
    val baseUrl = wireMock.baseUrl();
    val tokenKey = "0123456789abcdef0123456789abcdef".getBytes();

    stubFor(
        get(urlPathEqualTo("/fedauth"))
            .willReturn(
                badRequest()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"invalid_request\"}")));

    val client =
        GidClient.create(
                GidAuthenticationConfig.builder()
                    .fedAuthEndpoint(baseUrl + "/fedauth")
                    .tokenEndpoint(baseUrl + "/token")
                    .sekIdpIss("https://example.org/idp")
                    .clientId("eRezeptApp")
                    .redirectUri("https://redirect.example.org/callback")
                    .codeChallenge("test-code-challenge")
                    .codeVerifier("test-code-verifier")
                    .keyVerifier("test-key-verifier")
                    .tokenKey(tokenKey)
                    .scope(Scope.openId())
                    .kvnr("X110411675")
                    .build())
            .initialize();

    val ex = assertThrows(AuthException.class, client::login);

    assertTrue(ex.getMessage().contains("Missing Location header"), ex.getMessage());
    verify(1, getRequestedFor(urlPathEqualTo("/fedauth")));
    verify(0, getRequestedFor(urlPathEqualTo("/gsi-auth")));
    verify(0, postRequestedFor(urlPathEqualTo("/fedauth")));
    verify(0, postRequestedFor(urlPathEqualTo("/token")));
  }
}
