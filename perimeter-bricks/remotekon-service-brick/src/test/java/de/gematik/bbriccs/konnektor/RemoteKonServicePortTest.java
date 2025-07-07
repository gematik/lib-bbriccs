/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.bbriccs.konnektor;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.sun.xml.ws.client.ClientTransportException;
import de.gematik.bbriccs.cfg.dto.TLSConfiguration;
import de.gematik.bbriccs.konnektor.cfg.KonnektorContextConfiguration;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import java.net.MalformedURLException;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@WireMockTest
class RemoteKonServicePortTest {

  @RegisterExtension
  static WireMockExtension wm1 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  private static String url;

  @BeforeAll
  static void setup() {
    int port = wm1.getPort();
    url = "http://localhost:" + port;
  }

  static Stream<Arguments> shouldProvideServicePortType() {
    return Stream.of(
            (Function<RemoteKonServicePort, SignatureServicePortType>)
                RemoteKonServicePort::getSignatureService,
            (Function<RemoteKonServicePort, AuthSignatureServicePortType>)
                RemoteKonServicePort::getAuthSignatureService,
            (Function<RemoteKonServicePort, CertificateServicePortType>)
                RemoteKonServicePort::getCertificateService,
            (Function<RemoteKonServicePort, EventServicePortType>)
                RemoteKonServicePort::getEventService,
            (Function<RemoteKonServicePort, CardServicePortType>)
                RemoteKonServicePort::getCardService,
            (Function<RemoteKonServicePort, CardTerminalServicePortType>)
                RemoteKonServicePort::getCardTerminalService,
            (Function<RemoteKonServicePort, VSDServicePortType>)
                RemoteKonServicePort::getVSDServicePortType,
            (Function<RemoteKonServicePort, EncryptionServicePortType>)
                RemoteKonServicePort::getEncryptionServicePortType)
        .map(Arguments::of);
  }

  @SneakyThrows
  private void prepareKonnektorSdsResponse() {
    val sds = ResourceLoader.readFileFromResource("sds/cgm_connector_sds.xml");
    wm1.stubFor(get(urlEqualTo("/connector.sds")).willReturn(aResponse().withBody(sds)));
  }

  @Test
  void shouldFetchSdsOnConnect() {
    prepareKonnektorSdsResponse();
    val servicePort = RemoteKonServicePort.onRemote(url).build();
    val info = servicePort.toString();
    assertTrue(info.contains(url));

    val sds = assertDoesNotThrow(servicePort::getSds);
    assertTrue(sds.getProductName().contains("KoCoBox")); // CGM Konnektor/Product name
  }

  @Test
  void shouldReThrowOnMalformedUrl() {
    assertThrows(MalformedURLException.class, () -> RemoteKonServicePort.onRemote("abc"));
  }

  @ParameterizedTest
  @MethodSource
  <T> void shouldProvideServicePortType(Function<RemoteKonServicePort, T> serviceFetcher) {
    prepareKonnektorSdsResponse();
    val servicePort = RemoteKonServicePort.onRemote(url).build();

    val portType = assertDoesNotThrow(() -> serviceFetcher.apply(servicePort));
    assertNotNull(portType);
  }

  @ParameterizedTest
  @MethodSource("shouldProvideServicePortType")
  <T> void shouldProvideServicePortType02(Function<RemoteKonServicePort, T> serviceFetcher) {
    val cfg = new TLSConfiguration();
    cfg.setKeyStore("resources/tls/keystore.p12");
    cfg.setKeyStorePassword("changeit");

    cfg.setTrustStore("resource/tls/truststore.p12");
    cfg.setTrustStorePassword("123456");

    prepareKonnektorSdsResponse();
    val servicePort =
        RemoteKonServicePort.onRemote(url)
            .auth("user", "password")
            .hostnameVerifier((hostname, session) -> true)
            .trustProvider(TrustProvider.from(cfg))
            .build();

    val portType = assertDoesNotThrow(() -> serviceFetcher.apply(servicePort));
    assertNotNull(portType);
  }

  @Test
  void shouldVerifyHostname() {
    val cfg = new TLSConfiguration();
    cfg.setKeyStore("resources/tls/keystore.p12");
    cfg.setKeyStorePassword("changeit");

    cfg.setTrustStore("resource/tls/truststore.p12");
    cfg.setTrustStorePassword("123456");

    prepareKonnektorSdsResponse();
    val servicePort =
        RemoteKonServicePort.onRemote(url)
            .auth("user", "password")
            .hostnameVerifier((hostname, session) -> true)
            .trustProvider(TrustProvider.from(cfg))
            .build();

    val ctx = KonnektorContextConfiguration.getDefaultContextType();

    // the provided truststore won't verify for WireMock running on localhost!!
    val service = servicePort.getSignatureService();
    assertThrows(ClientTransportException.class, () -> service.getJobNumber(ctx));
  }
}
