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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.crypto.BC;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.idp.exception.AuthException;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.idp.client.AuthorizationCodeResult;
import de.gematik.idp.client.IdpClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

class OwnerAccessTokenHeaderProviderTest {

  private static final String AUTH_SERVICE = "https://auth.example";
  private static final String CALLBACK = AUTH_SERVICE + "/signin-gematik-idp-dienst";
  private static final String REDIRECT =
      "https://idp.example/auth?response_type=code&client_id=vzd-client"
          + "&scope=fhir-vzd+openid"
          + "&redirect_uri=https%3A%2F%2Fauth.example%2Fsignin-gematik-idp-dienst"
          + "&state=original-state&code_challenge=pkce-challenge&code_challenge_method=S256";

  @ParameterizedTest
  @EnumSource(
      value = CryptoSystem.class,
      names = {"ECC_256", "RSA_2048"})
  void shouldAuthenticateAndReturnOwnerToken(CryptoSystem cryptoSystem) throws Exception {
    val aut =
        SmartcardArchive.fromResources().getSmcB(0).getAutCertificate(cryptoSystem).orElseThrow();
    val smartcard = mock(Smartcard.class);
    when(smartcard.getAutCertificate()).thenReturn(aut);
    val httpClient = mock(HttpClient.class);
    val httpBuilder = mock(HttpClient.Builder.class, RETURNS_SELF);
    when(httpBuilder.build()).thenReturn(httpClient);
    val idp = mock(IdpClient.class);
    val idpBuilder = mock(IdpClient.IdpClientBuilder.class, RETURNS_SELF);
    when(idpBuilder.build()).thenReturn(idp);

    val authCode = new AuthorizationCodeResult("code+/=&", "state+/=&", CALLBACK);
    val content = "header.payload".getBytes(StandardCharsets.UTF_8);
    when(idp.login(
            eq(aut.getX509Certificate()),
            any(),
            eq("pkce-challenge"),
            eq("original-state"),
            anyString()))
        .thenAnswer(
            invocation -> {
              UnaryOperator<byte[]> signer = invocation.getArgument(1);
              val signature = signer.apply(content);
              val verifier =
                  Signature.getInstance(
                      cryptoSystem == CryptoSystem.ECC_256 ? "SHA256withECDSA" : "RSASSA-PSS",
                      BC.getSecurityProvider());
              if (cryptoSystem == CryptoSystem.RSA_2048) {
                verifier.setParameter(
                    new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
              }
              verifier.initVerify(aut.getX509Certificate().getPublicKey());
              verifier.update(content);
              assertTrue(verifier.verify(signature));
              assertFalse(invocation.<String>getArgument(4).isBlank());
              return authCode;
            });

    HttpResponse<String> redirectResponse = mock();
    when(redirectResponse.headers())
        .thenReturn(HttpHeaders.of(Map.of("Location", List.of(REDIRECT)), (key, value) -> true));
    HttpResponse<String> tokenResponse = mock();
    when(tokenResponse.body())
        .thenReturn(
            """
            {"access_token":"owner-token","token_type":"Bearer","expires_in":3600}
            """);
    when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any()))
        .thenReturn(redirectResponse, tokenResponse);

    try (val httpFactory = mockStatic(HttpClient.class);
        val idpFactory = mockStatic(IdpClient.class)) {
      httpFactory.when(HttpClient::newBuilder).thenReturn(httpBuilder);
      idpFactory.when(IdpClient::builder).thenReturn(idpBuilder);
      val provider =
          OwnerAccessTokenHeaderProvider.builder()
              .withAuthService(AUTH_SERVICE)
              .withSmartcard(smartcard)
              .build();

      val headers = provider.forRequest(HttpBRequest.get().urlPath("/search").withoutPayload());

      assertEquals(List.of(JwtHeaderKey.AUTHORIZATION.createHeader("owner-token")), headers);
      val requests = ArgumentCaptor.forClass(HttpRequest.class);
      verify(httpClient, times(2))
          .send(requests.capture(), ArgumentMatchers.<BodyHandler<String>>any());
      assertEquals(
          URI.create(AUTH_SERVICE + "/owner-authenticate"), requests.getAllValues().get(0).uri());
      assertEquals(
          URI.create(CALLBACK + "?code=code%2B%2F%3D%26&state=state%2B%2F%3D%26"),
          requests.getAllValues().get(1).uri());
      assertTrue(
          requests.getAllValues().stream().allMatch(request -> request.method().equals("GET")));
      verify(httpBuilder).followRedirects(HttpClient.Redirect.NEVER);
      verify(idpBuilder).clientId("vzd-client");
      verify(idpBuilder).redirectUrl(CALLBACK);
      verify(idpBuilder)
          .discoveryDocumentUrl("https://idp.example/.well-known/openid-configuration");
      verify(idpBuilder).scopes(Set.of("fhir-vzd", "openid"));
      verify(idp).initialize();
    }
  }

  @Test
  void shouldReuseOwnerTokenOnImmediateSecondRequest() throws Exception {
    val aut =
        SmartcardArchive.fromResources()
            .getSmcB(0)
            .getAutCertificate(CryptoSystem.ECC_256)
            .orElseThrow();
    val smartcard = mock(Smartcard.class);
    when(smartcard.getAutCertificate()).thenReturn(aut);
    val httpClient = mock(HttpClient.class);
    val httpBuilder = mock(HttpClient.Builder.class, RETURNS_SELF);
    when(httpBuilder.build()).thenReturn(httpClient);
    val idp = mock(IdpClient.class);
    val idpBuilder = mock(IdpClient.IdpClientBuilder.class, RETURNS_SELF);
    when(idpBuilder.build()).thenReturn(idp);
    when(idp.login(any(), any(), anyString(), anyString(), anyString()))
        .thenReturn(new AuthorizationCodeResult("code", "original-state", CALLBACK));

    HttpResponse<String> redirectResponse = mock();
    when(redirectResponse.headers())
        .thenReturn(HttpHeaders.of(Map.of("Location", List.of(REDIRECT)), (key, value) -> true));
    HttpResponse<String> tokenResponse = mock();
    when(tokenResponse.body())
        .thenReturn(
            """
            {"access_token":"owner-token","token_type":"Bearer","expires_in":3600}
            """);
    when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any()))
        .thenReturn(redirectResponse, tokenResponse);

    try (val httpFactory = mockStatic(HttpClient.class);
        val idpFactory = mockStatic(IdpClient.class)) {
      httpFactory.when(HttpClient::newBuilder).thenReturn(httpBuilder);
      idpFactory.when(IdpClient::builder).thenReturn(idpBuilder);
      val provider =
          OwnerAccessTokenHeaderProvider.builder()
              .withAuthService(AUTH_SERVICE)
              .withSmartcard(smartcard)
              .build();
      val request = HttpBRequest.get().urlPath("/search").withoutPayload();

      val firstHeaders = provider.forRequest(request);
      assertEquals(List.of(JwtHeaderKey.AUTHORIZATION.createHeader("owner-token")), firstHeaders);
      verify(httpClient, times(2))
          .send(any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any());
      verify(idp).login(any(), any(), anyString(), anyString(), anyString());
      clearInvocations(httpClient, idp, smartcard);

      val secondHeaders = provider.forRequest(request);

      assertEquals(firstHeaders, secondHeaders);
      verifyNoInteractions(httpClient, idp, smartcard);
    }
  }

  @Test
  void shouldRejectMissingAuthorizationRedirect() throws Exception {
    val httpClient = mock(HttpClient.class);
    val httpBuilder = mock(HttpClient.Builder.class, RETURNS_SELF);
    when(httpBuilder.build()).thenReturn(httpClient);
    HttpResponse<String> response = mock();
    when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (key, value) -> true));
    when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any()))
        .thenReturn(response);
    val smartcard = mock(Smartcard.class);

    try (val httpFactory = mockStatic(HttpClient.class)) {
      httpFactory.when(HttpClient::newBuilder).thenReturn(httpBuilder);
      val provider =
          OwnerAccessTokenHeaderProvider.builder()
              .withAuthService(AUTH_SERVICE)
              .withSmartcard(smartcard)
              .build();

      val req = HttpBRequest.get().urlPath("/search").withoutPayload();
      val exception = assertThrows(AuthException.class, () -> provider.forRequest(req));

      assertTrue(exception.getMessage().contains("No Location header"));
      verifyNoInteractions(smartcard);
      verify(httpClient).send(any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any());
    }
  }

  @Test
  void shouldRequireNonNullAuthServiceUrl() {
    val builder = OwnerAccessTokenHeaderProvider.builder();
    assertThrows(NullPointerException.class, () -> builder.withAuthService(null));
  }

  @Test
  void shouldRejectRelativeAuthServiceUrl() {
    val builder = OwnerAccessTokenHeaderProvider.builder();
    assertThrows(
        IllegalArgumentException.class, () -> builder.withAuthService("/owner-authenticate"));
  }
}
