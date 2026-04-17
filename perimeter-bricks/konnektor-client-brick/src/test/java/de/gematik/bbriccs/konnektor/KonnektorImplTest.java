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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.cardterminal.CardTerminal;
import de.gematik.bbriccs.cardterminal.CardTerminalSlot;
import de.gematik.bbriccs.cardterminal.exceptions.NoFreeSlotException;
import de.gematik.bbriccs.konnektor.cfg.KonnektorContextConfiguration;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KonnektorImplTest {

  private ServicePort servicePort;
  private EventServicePortType eventService;
  private CardTerminal cardTerminal;
  private KonnektorImpl konnektor;

  @BeforeEach
  void setup() {
    servicePort = mock(ServicePort.class);
    val sds = KonnektorServiceDefinition.builder().productName("Test-Konnektor").build();
    when(servicePort.getSds()).thenReturn(sds);

    eventService = mock(EventServicePortType.class);
    when(servicePort.getEventService()).thenReturn(eventService);

    cardTerminal = mock(CardTerminal.class);
    when(cardTerminal.getCtId()).thenReturn("ct-1");
    when(cardTerminal.getAllSlots()).thenReturn(List.of());

    konnektor =
        new KonnektorImpl(
            KonnektorContextConfiguration.getDefaultContextType(),
            servicePort,
            List.of(cardTerminal));
  }

  @Test
  @SneakyThrows
  void shouldInsertCardToFreeSlotWhenAvailable() {
    val freeSlot = new CardTerminalSlot(1);
    when(cardTerminal.getFreeSlot()).thenReturn(Optional.of(freeSlot));
    when(cardTerminal.hasFreeSlot()).thenReturn(true);

    val card = mock(Smartcard.class);
    when(card.getIccsn()).thenReturn("test-iccsn");

    assertDoesNotThrow(() -> konnektor.insertCard(card));

    verify(cardTerminal).insertCard(card);
    verify(eventService, never()).getCards(any());
  }

  @Test
  @SneakyThrows
  void shouldReplaceOldestCardViaGetCardsWhenNoFreeSlotAvailable() {
    when(cardTerminal.getFreeSlot()).thenReturn(Optional.empty());
    when(cardTerminal.hasFreeSlot()).thenReturn(false);

    val card = mock(Smartcard.class);
    when(card.getIccsn()).thenReturn("new-iccsn");

    // newer card in slot 1, older card in slot 2 – slot 2 should be replaced
    val newerCit = createCardInfoType("ct-1", 1, ZonedDateTime.now().minusHours(1));
    val olderCit = createCardInfoType("ct-1", 2, ZonedDateTime.now().minusDays(5));

    val cardsResponse = buildGetCardsResponse(newerCit, olderCit);
    when(eventService.getCards(any())).thenReturn(cardsResponse);

    assertDoesNotThrow(() -> konnektor.insertCard(card));

    verify(cardTerminal).insertCard(card, 2);
    verify(cardTerminal, never()).insertCard(card);
  }

  @Test
  @SneakyThrows
  void shouldThrowNoFreeSlotExceptionWhenNoFreeSlotAndNoInsertTimesKnown() {
    when(cardTerminal.getFreeSlot()).thenReturn(Optional.empty());
    when(cardTerminal.hasFreeSlot()).thenReturn(false);

    val card = mock(Smartcard.class);
    when(card.getIccsn()).thenReturn("new-iccsn");

    // Cards without insertTime set
    val cit = new CardInfoType();
    cit.setCtId("ct-1");
    cit.setSlotId(BigInteger.ONE);

    val cardsResponse = buildGetCardsResponse(cit);
    when(eventService.getCards(any())).thenReturn(cardsResponse);

    assertThrows(NoFreeSlotException.class, () -> konnektor.insertCard(card));
  }

  @SneakyThrows
  private static CardInfoType createCardInfoType(
      String ctId, int slotId, ZonedDateTime insertTime) {
    val cit = new CardInfoType();
    cit.setCtId(ctId);
    cit.setSlotId(BigInteger.valueOf(slotId));
    val gc = GregorianCalendar.from(insertTime);
    XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    cit.setInsertTime(xgc);
    return cit;
  }

  private static GetCardsResponse buildGetCardsResponse(CardInfoType... cardInfoTypes) {
    val cards = new Cards();
    cards.getCard().addAll(List.of(cardInfoTypes));
    val response = new GetCardsResponse();
    response.setCards(cards);
    return response;
  }
}
