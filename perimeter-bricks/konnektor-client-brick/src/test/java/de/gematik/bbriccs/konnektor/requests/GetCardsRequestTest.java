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

package de.gematik.bbriccs.konnektor.requests;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import jakarta.xml.bind.JAXBContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class GetCardsRequestTest {

  @SneakyThrows
  private static GetCardsResponse parseGetCardsResponse(String resourcePath) {
    val stream = ResourceLoader.getFileFromResourceAsStream(resourcePath);
    val reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    val jaxb =
        JAXBContext.newInstance(
            "de.gematik.ws.conn.eventservice.v7"
                + ":de.gematik.ws.conn.cardservice.v8"
                + ":de.gematik.ws.conn.cardservicecommon.v2"
                + ":de.gematik.ws.conn.connectorcommon.v5");
    val unmarshaller = jaxb.createUnmarshaller();

    // SOAP-Envelope entfernen – nur das innere GetCardsResponse-Element extrahieren
    val sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    val xml = sb.toString();
    val start = xml.indexOf("<ns6:GetCardsResponse");
    val end = xml.lastIndexOf("</ns6:GetCardsResponse>") + "</ns6:GetCardsResponse>".length();
    val responseXml = xml.substring(start, end);
    return (GetCardsResponse) unmarshaller.unmarshal(new StringReader(responseXml));
  }

  @Test
  void shouldParseGetCardsResponseWithCardsNotNull() {
    val response = parseGetCardsResponse("soap/get_cards_response.xml");
    assertNotNull(response, "GetCardsResponse darf nicht null sein");
    assertNotNull(response.getStatus(), "Status darf nicht null sein");
    assertEquals("OK", response.getStatus().getResult(), "Status sollte OK sein");
    assertNotNull(response.getCards(), "Cards darf nicht null sein");
  }

  @Test
  void shouldParseAllFourCardsFromResponse() {
    val response = parseGetCardsResponse("soap/get_cards_response.xml");
    assertNotNull(response.getCards());
    val cards = response.getCards().getCard();
    assertEquals(4, cards.size(), "Es sollten 4 Karten im Response enthalten sein");
  }

  @Test
  void shouldParseSmbCards() {
    val response = parseGetCardsResponse("soap/get_cards_response.xml");
    val smbCards =
        response.getCards().getCard().stream()
            .filter(c -> CardTypeType.SMC_B.equals(c.getCardType()))
            .toList();
    assertEquals(2, smbCards.size(), "Es sollten 2 SMC-B Karten vorhanden sein");
    assertTrue(smbCards.stream().anyMatch(c -> "SMC-B-6".equals(c.getCardHandle())));
    assertTrue(smbCards.stream().anyMatch(c -> "SMC-B-7".equals(c.getCardHandle())));
  }

  @Test
  void shouldParseHbaCards() {
    val response = parseGetCardsResponse("soap/get_cards_response.xml");
    val hbaCards =
        response.getCards().getCard().stream()
            .filter(c -> CardTypeType.HBA.equals(c.getCardType()))
            .toList();
    assertEquals(2, hbaCards.size(), "Es sollten 2 HBA Karten vorhanden sein");
    assertTrue(hbaCards.stream().anyMatch(c -> "HBA-2".equals(c.getCardHandle())));
    assertTrue(hbaCards.stream().anyMatch(c -> "HBA-4".equals(c.getCardHandle())));
  }

  @Test
  void shouldParseIccsnFromCards() {
    val response = parseGetCardsResponse("soap/get_cards_response.xml");
    val iccsnList = response.getCards().getCard().stream().map(c -> c.getIccsn()).toList();
    assertTrue(iccsnList.contains("80276883110000163973"));
    assertTrue(iccsnList.contains("80276883110000163972"));
    assertTrue(iccsnList.contains("80276883110000161759"));
    assertTrue(iccsnList.contains("80276883110000170943"));
  }
}
