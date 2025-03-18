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
 */

package de.gematik.bbriccs.konnektor;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.cats.cfg.CatsConfiguration;
import de.gematik.bbriccs.cfg.dto.TLSConfiguration;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.cfg.KonnektorContextConfiguration;
import de.gematik.bbriccs.konnektor.cfg.RemoteKonServiceConfiguration;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.net.MalformedURLException;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest
class RemoteKonFactoryTest {

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

  @SneakyThrows
  private void prepareKonnektorSdsResponse() {
    val sds = ResourceLoader.readFileFromResource("sds/cgm_connector_sds.xml");
    wm1.stubFor(get(urlEqualTo("/connector.sds")).willReturn(aResponse().withBody(sds)));
  }

  @Test
  void shouldMapConfigToBuildInstruction() {
    prepareKonnektorSdsResponse();

    val tlsCfg = new TLSConfiguration();
    tlsCfg.setKeyStore("resources/tls/keystore.p12");
    tlsCfg.setKeyStorePassword("changeit");

    tlsCfg.setTrustStore("resource/tls/truststore.p12");
    tlsCfg.setTrustStorePassword("123456");

    val serviceCfg = new RemoteKonServiceConfiguration();
    serviceCfg.setAddress(url);
    serviceCfg.setTls(tlsCfg);

    val ctx = KonnektorContextConfiguration.getDefaultContextConfiguration();

    val ctCfg = new CatsConfiguration();
    ctCfg.setType("CATS");
    ctCfg.setCtId("ctId");
    ctCfg.setUrl(url);

    val cfg = new KonnektorConfiguration();
    cfg.setService(serviceCfg);
    cfg.setContext(ctx);
    cfg.setCardTerminals(List.of(ctCfg));

    val factory = new RemoteKonFactory();
    val bi = assertDoesNotThrow(() -> factory.mapConfiguration(cfg));
    assertNotNull(bi.getCtx());
    assertEquals(1, bi.getCardTerminals().size());
    val catsCt = assertDoesNotThrow(() -> bi.getCardTerminals().stream().findFirst().orElseThrow());
    assertEquals("ctId", catsCt.getCtId());
  }

  @Test
  void shouldReThrowOnInvalidUrl() {
    val serviceCfg = new RemoteKonServiceConfiguration();
    serviceCfg.setAddress("abc");
    val cfg = new KonnektorConfiguration();
    cfg.setService(serviceCfg);

    val factory = new RemoteKonFactory();
    assertEquals(RemoteKonFactory.SERVICE_NAME, factory.getType());
    assertThrows(MalformedURLException.class, () -> factory.mapConfiguration(cfg));
  }
}
