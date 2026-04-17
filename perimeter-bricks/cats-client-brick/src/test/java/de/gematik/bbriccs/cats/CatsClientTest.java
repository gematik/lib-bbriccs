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
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalException;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

@WireMockTest
class CatsClientTest {

  @SneakyThrows
  private void preparePositiveCatsMock() {
    stubFor(
        put(urlPathMatching("/config/card/slot/\\d+")).willReturn(aResponse().withBody("good")));

    stubFor(post(urlEqualTo("/config/card/insert")).willReturn(aResponse().withBody("good")));

    stubFor(post(urlEqualTo("/config/card/startedState")).willReturn(aResponse().withBody("good")));

    stubFor(post(urlEqualTo("/config/card/slots/reset")).willReturn(aResponse().withBody("good")));

    stubFor(
        get(urlPathMatching("/config/card/configurationFile/\\d+"))
            .willReturn(aResponse().withBody("")));
  }

  @Test
  void shouldInsertCardToSlot(WireMockRuntimeInfo wireMockRuntimeInfo) {
    val sca = SmartcardArchive.fromResources();
    val url = wireMockRuntimeInfo.getHttpBaseUrl();

    preparePositiveCatsMock();
    val catsClient = CatsClient.create(url).configPath("a/b/c").withTerminalId("001").connect();
    assertDoesNotThrow(() -> catsClient.insertCard(sca.getEgk(0), 1));
    assertDoesNotThrow(catsClient::disconnect);
  }

  @Test
  void shouldInsertCardToNextFreeSlot(WireMockRuntimeInfo wireMockRuntimeInfo) {
    val sca = SmartcardArchive.fromResources();
    val url = wireMockRuntimeInfo.getHttpBaseUrl();

    preparePositiveCatsMock();
    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertDoesNotThrow(() -> catsClient.insertCard(sca.getEgk(0)));
  }

  @Test
  void shouldResetAllSlots(WireMockRuntimeInfo wireMockRuntimeInfo) {
    val url = wireMockRuntimeInfo.getHttpBaseUrl();

    preparePositiveCatsMock();
    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertDoesNotThrow(catsClient::resetSlots);
  }

  @Test
  void shouldThrowOnErrorWhileInsert(WireMockRuntimeInfo wireMockRuntimeInfo) {
    val sca = SmartcardArchive.fromResources();
    val egk = sca.getEgk(0);
    val url = wireMockRuntimeInfo.getHttpBaseUrl();

    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertThrows(CardTerminalException.class, () -> catsClient.insertCard(egk, 0));
  }

  @Test
  void shouldThrowOnInsertToUnknownSlot(WireMockRuntimeInfo wireMockRuntimeInfo) {
    val sca = SmartcardArchive.fromResources();
    val egk = sca.getEgk(0);
    val url = wireMockRuntimeInfo.getHttpBaseUrl();

    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    assertThrows(CardTerminalException.class, () -> catsClient.insertCard(egk, 1000));
  }

  @Test
  void shouldParseIccsnFromOccupiedSlot(WireMockRuntimeInfo wireMockRuntimeInfo) {
    val url = wireMockRuntimeInfo.getHttpBaseUrl();
    val expectedIccsn = "80276883110000170943";

    stubFor(
        get(urlPathMatching("/config/card/configurationFile/\\d+"))
            .willReturn(aResponse().withBody("configuration_hba_" + expectedIccsn + ".xml")));

    val catsClient = CatsClient.create(url).withTerminalId("001").connect();
    val slots = catsClient.getAllSlots();

    assertTrue(slots.stream().allMatch(s -> s.getIccsn().isPresent()));
    assertTrue(slots.stream().allMatch(s -> expectedIccsn.equals(s.getIccsn().get())));
  }
}
