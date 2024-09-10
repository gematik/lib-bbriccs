/*
 * Copyright 2024 gematik GmbH
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
 */

package de.gematik.bbriccs.rest.idp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.fd.FdRequest;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.crypto.EcSignerUtility;
import de.gematik.idp.crypto.RsaSignerUtility;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.idp.token.JsonWebToken;
import java.security.interfaces.RSAPrivateKey;
import java.util.function.UnaryOperator;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IdpTokenHeaderProviderTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setupSmartcards() {
    sca = SmartcardArchive.fromResources();
  }

  @Test
  void shouldAuthenticateViaSmartcardWithCaching() {
    val egk = sca.getEgk(0);

    val pb =
        IdpTokenHeaderProvider.withDiscoveryDocumentUrl(
                "https://idp-ref.app.ti-dienste.de/.well-known/openid-configuration")
            .withRedirectUrl("https://test-ps.gematik.de/bbriccs")
            .withClientId("bbriccsTestPs")
            .usingScope("bbriccs");

    try (val staticMockIdpClient = mockStatic(IdpClient.class)) {
      val mockIdpClient = mock(IdpClient.class);
      val mockBuilder = mock(IdpClient.IdpClientBuilder.class);
      when(mockBuilder.redirectUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.discoveryDocumentUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.scopes(anySet())).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockIdpClient);
      staticMockIdpClient.when(IdpClient::builder).thenReturn(mockBuilder);

      val idpTokenResult1 = IdpTokenResult.builder().accessToken(new JsonWebToken("ABC")).build();
      idpTokenResult1.setExpiresIn(1000);
      val idpTokenResult2 = IdpTokenResult.builder().accessToken(new JsonWebToken("XYZ")).build();
      when(mockIdpClient.login(any())).thenReturn(idpTokenResult1).thenReturn(idpTokenResult2);

      val provider = assertDoesNotThrow(() -> pb.authenticateWith(egk));

      val request = createMockRequest();
      val firstHeader = assertDoesNotThrow(() -> provider.forRequest(request));
      assertEquals(JwtHeaderKey.AUTHORIZATION.getKey(), firstHeader.key());
      assertNotNull(firstHeader.value());

      val secondHeader = assertDoesNotThrow(() -> provider.forRequest(request));
      assertEquals(firstHeader.value(), secondHeader.value());
    }
  }

  @Test
  void shouldRefreshIfTokenExpired() {
    val egk = sca.getEgk(0);

    val pb =
        IdpTokenHeaderProvider.withDiscoveryDocumentUrl(
                "https://idp-ref.app.ti-dienste.de/.well-known/openid-configuration")
            .withRedirectUrl("https://test-ps.gematik.de/bbriccs")
            .withClientId("bbriccsTestPs")
            .usingScope("bbriccs");

    try (val staticMockIdpClient = mockStatic(IdpClient.class)) {
      val mockIdpClient = mock(IdpClient.class);
      val mockBuilder = mock(IdpClient.IdpClientBuilder.class);
      when(mockBuilder.redirectUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.discoveryDocumentUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.scopes(anySet())).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockIdpClient);
      staticMockIdpClient.when(IdpClient::builder).thenReturn(mockBuilder);

      val idpTokenResult1 = IdpTokenResult.builder().accessToken(new JsonWebToken("ABC")).build();
      idpTokenResult1.setExpiresIn(0);
      val idpTokenResult2 = IdpTokenResult.builder().accessToken(new JsonWebToken("XYZ")).build();
      when(mockIdpClient.login(any())).thenReturn(idpTokenResult1).thenReturn(idpTokenResult2);

      val provider = assertDoesNotThrow(() -> pb.authenticateWith(egk));

      val request = createMockRequest();
      val firstHeader = assertDoesNotThrow(() -> provider.forRequest(request));
      assertEquals(JwtHeaderKey.AUTHORIZATION.getKey(), firstHeader.key());
      assertNotNull(firstHeader.value());
      assertEquals("Bearer ABC", firstHeader.value());

      val secondHeader = assertDoesNotThrow(() -> provider.forRequest(request));
      assertNotNull(secondHeader.value());
      assertNotEquals(firstHeader.value(), secondHeader.value());
      assertEquals("Bearer XYZ", secondHeader.value());
    }
  }

  @Test
  void shouldAuthenticateViaExternalAuthenticate() {
    val egk = sca.getEgk(0);

    val pb =
        IdpTokenHeaderProvider.withDiscoveryDocumentUrl(
                "https://idp-ref.app.ti-dienste.de/.well-known/openid-configuration")
            .withRedirectUrl("https://test-ps.gematik.de/bbriccs")
            .withClientId("bbriccsTestPs")
            .usingScope("bbriccs");

    try (val staticMockIdpClient = mockStatic(IdpClient.class)) {
      val mockIdpClient = mock(IdpClient.class);
      val mockBuilder = mock(IdpClient.IdpClientBuilder.class);
      when(mockBuilder.redirectUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.discoveryDocumentUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.scopes(anySet())).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockIdpClient);
      staticMockIdpClient.when(IdpClient::builder).thenReturn(mockBuilder);

      val idpTokenResult1 = IdpTokenResult.builder().accessToken(new JsonWebToken("ABC")).build();
      idpTokenResult1.setExpiresIn(1000);
      val idpTokenResult2 = IdpTokenResult.builder().accessToken(new JsonWebToken("XYZ")).build();
      when(mockIdpClient.login(any(), any()))
          .thenReturn(idpTokenResult1)
          .thenReturn(idpTokenResult2);

      val autCertificate = egk.getAutCertificate();
      val pki =
          PkiIdentity.builder()
              .certificate(autCertificate.getX509Certificate())
              .privateKey(autCertificate.getPrivateKey())
              .build();
      UnaryOperator<byte[]> challenge =
          (tbsData) ->
              pki.getPrivateKey() instanceof RSAPrivateKey
                  ? RsaSignerUtility.createRsaSignature(tbsData, pki.getPrivateKey())
                  : EcSignerUtility.createEcSignature(tbsData, pki.getPrivateKey());
      val provider =
          assertDoesNotThrow(
              () -> pb.authenticateWith(autCertificate.getX509Certificate(), challenge));

      val request = createMockRequest();
      val firstHeader = assertDoesNotThrow(() -> provider.forRequest(request));
      assertEquals(JwtHeaderKey.AUTHORIZATION.getKey(), firstHeader.key());
      assertNotNull(firstHeader.value());

      val secondHeader = assertDoesNotThrow(() -> provider.forRequest(request));
      assertEquals(firstHeader.value(), secondHeader.value());
    }
  }

  @Test
  void shouldCatchNPEsFromIdpClient() {
    val egk = sca.getEgk(0);

    val pb =
        IdpTokenHeaderProvider.withDiscoveryDocumentUrl(
                "https://idp-ref.app.ti-dienste.de/.well-known/openid-configuration")
            .withRedirectUrl("https://test-ps.gematik.de/bbriccs")
            .withClientId("bbriccsTestPs")
            .usingScope("bbriccs");

    try (val staticMockIdpClient = mockStatic(IdpClient.class)) {
      val mockIdpClient = mock(IdpClient.class);
      val mockBuilder = mock(IdpClient.IdpClientBuilder.class);
      when(mockBuilder.redirectUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.discoveryDocumentUrl(anyString())).thenReturn(mockBuilder);
      when(mockBuilder.scopes(anySet())).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockIdpClient);
      staticMockIdpClient.when(IdpClient::builder).thenReturn(mockBuilder);

      when(mockIdpClient.login(any())).thenThrow(NullPointerException.class);

      val provider = assertDoesNotThrow(() -> pb.authenticateWith(egk));

      val request = createMockRequest();
      assertThrows(
          IdpClientRuntimeException.class,
          () -> provider.forRequest(request)); // instead of the NPE
    }
  }

  @SuppressWarnings("unchecked")
  private FdRequest<EmptyResource, Bundle> createMockRequest() {
    return (FdRequest<EmptyResource, Bundle>) mock(FdRequest.class);
  }
}
