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

package de.gematik.bbriccs.cats;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalException;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest
class CatsClientTest {

  @RegisterExtension
  static WireMockExtension wm1 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  private static String url;

  @BeforeAll
  static void setup() {
    url = "http://localhost:" + wm1.getPort();
  }

  @SneakyThrows
  private void preparePositiveCatsMock() {
    wm1.stubFor(post(urlEqualTo("/config/card/insert")).willReturn(aResponse().withBody("good")));

    wm1.stubFor(
        post(urlEqualTo("/config/card/configuration")).willReturn(aResponse().withBody("good")));
  }

  @Test
  void shouldInsertCardToSlot() {
    val sca = SmartcardArchive.fromResources();

    preparePositiveCatsMock();
    val catsClient = CatsClient.create(url).configPath("a/b/c").withTerminalId("001").connect();
    assertDoesNotThrow(() -> catsClient.insertCard(sca.getEgk(0), 0));
    assertDoesNotThrow(catsClient::disconnect);
  }

  @Test
  void shouldInsertCardToNextFreeSlot() {
    val sca = SmartcardArchive.fromResources();

    preparePositiveCatsMock();
    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertDoesNotThrow(() -> catsClient.insertCard(sca.getEgk(0)));
  }

  @Test
  void shouldResetAllSlots() {
    val sca = SmartcardArchive.fromResources();

    preparePositiveCatsMock();
    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertDoesNotThrow(catsClient::resetSlots);
  }

  @Test
  void shouldThrowOnErrorWhileInsert() {
    val sca = SmartcardArchive.fromResources();
    val egk = sca.getEgk(0);

    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertThrows(CardTerminalException.class, () -> catsClient.insertCard(egk, 0));
  }

  @Test
  void shouldThrowOnInsertToUnknownSlot() {
    val sca = SmartcardArchive.fromResources();
    val egk = sca.getEgk(0);

    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertThrows(CardTerminalException.class, () -> catsClient.insertCard(egk, 1000));
  }
}
