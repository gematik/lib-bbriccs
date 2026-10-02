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

package de.gematik.bbriccs.cardterminal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalNotFoundException;
import de.gematik.bbriccs.cardterminal.exceptions.NoFreeSlotException;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class CardTerminalOperatorTest {

  @Test
  void shouldHaveFreeSlotWhenAnyCardTerminalHasFreeSlot() {
    val cardTerminal1 = mock(CardTerminal.class);
    when(cardTerminal1.hasFreeSlot()).thenReturn(false);

    val cardTerminal2 = mock(CardTerminal.class);
    when(cardTerminal2.hasFreeSlot()).thenReturn(true);

    val operator = new CardTerminalOperator(List.of(cardTerminal1, cardTerminal2));

    assertTrue(operator.hasFreeSlot());
  }

  @Test
  void shouldNotHaveFreeSlotWhenNoCardTerminalHasFreeSlot() {
    val cardTerminal1 = mock(CardTerminal.class);
    when(cardTerminal1.hasFreeSlot()).thenReturn(false);

    val cardTerminal2 = mock(CardTerminal.class);
    when(cardTerminal2.hasFreeSlot()).thenReturn(false);

    val operator = new CardTerminalOperator(List.of(cardTerminal1, cardTerminal2));

    assertFalse(operator.hasFreeSlot());
  }

  @Test
  void shouldExposeConfiguredCardTerminals() {
    val cardTerminal1 = mock(CardTerminal.class);
    val cardTerminal2 = mock(CardTerminal.class);

    val operator = new CardTerminalOperator(List.of(cardTerminal1, cardTerminal2));

    val configuredCardTerminals = operator.cardTerminals();
    assertEquals(2, configuredCardTerminals.size());
    assertTrue(configuredCardTerminals.contains(cardTerminal1));
    assertTrue(configuredCardTerminals.contains(cardTerminal2));
    assertThrows(
        UnsupportedOperationException.class,
        () -> configuredCardTerminals.add(mock(CardTerminal.class)));
  }

  @Test
  void shouldInsertSmartcard() {
    val sca = SmartcardArchive.fromResources();

    val cardTerminal = mock(CardTerminal.class);
    when(cardTerminal.hasFreeSlot()).thenReturn(true);

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal));
    assertDoesNotThrow(() -> operator.insertCard(egk));
    verify(cardTerminal, times(1)).insertCard(egk);
  }

  @Test
  void shouldInsertIntoFirstConfiguredTerminalWithFreeSlot() {
    val sca = SmartcardArchive.fromResources();

    val cardTerminal1 = mock(CardTerminal.class);
    when(cardTerminal1.hasFreeSlot()).thenReturn(true);

    val cardTerminal2 = mock(CardTerminal.class);
    when(cardTerminal2.hasFreeSlot()).thenReturn(true);

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal1, cardTerminal2));

    assertDoesNotThrow(() -> operator.insertCard(egk));

    verify(cardTerminal1, times(1)).insertCard(egk);
    verify(cardTerminal2, never()).insertCard(egk);
  }

  @Test
  void shouldInsertSmartcardToSpecificSlot() {
    val sca = SmartcardArchive.fromResources();

    val slot = new CardTerminalSlot(2);
    val cardTerminal = mock(CardTerminal.class);
    when(cardTerminal.getCtId()).thenReturn("ct-1");
    when(cardTerminal.getAllSlots()).thenReturn(List.of(slot));

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal));

    assertDoesNotThrow(() -> operator.insertCard(egk, "ct-1", 2));
    verify(cardTerminal, times(1)).insertCard(egk, 2);
  }

  @Test
  void shouldThrowOnNoFreeSlots() {
    val sca = SmartcardArchive.fromResources();

    val cardTerminal1 = mock(CardTerminal.class);
    when(cardTerminal1.hasFreeSlot()).thenReturn(false);

    val cardTerminal2 = mock(CardTerminal.class);
    when(cardTerminal2.hasFreeSlot()).thenReturn(false);

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal1, cardTerminal2));
    assertThrows(NoFreeSlotException.class, () -> operator.insertCard(egk));
  }

  @Test
  void shouldThrowOnNoFreeSpecificSlot() {
    val sca = SmartcardArchive.fromResources();

    val occupiedSlot = new CardTerminalSlot(2);
    occupiedSlot.setIccsn("occupied");

    val cardTerminal1 = mock(CardTerminal.class);
    when(cardTerminal1.getCtId()).thenReturn("ct-1");
    when(cardTerminal1.getAllSlots()).thenReturn(List.of(occupiedSlot));

    val cardTerminal2 = mock(CardTerminal.class);
    when(cardTerminal2.getCtId()).thenReturn("ct-2");
    when(cardTerminal2.getAllSlots()).thenReturn(List.of(new CardTerminalSlot(1)));

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal1, cardTerminal2));

    // "ct-3" is not among the configured terminals → CardTerminalNotFoundException expected
    assertThrows(CardTerminalNotFoundException.class, () -> operator.insertCard(egk, "ct-3", 2));
  }

  @Test
  void shouldThrowCardTerminalNotFoundExceptionWithTerminalIdInMessage() {
    val sca = SmartcardArchive.fromResources();
    val unknownCtId = "unknown-ct-99";

    val cardTerminal = mock(CardTerminal.class);
    when(cardTerminal.getCtId()).thenReturn("ct-1");
    when(cardTerminal.getAllSlots()).thenReturn(List.of());

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal));

    // the exception message must contain the requested terminal ID for diagnostics
    val ex =
        assertThrows(
            CardTerminalNotFoundException.class, () -> operator.insertCard(egk, unknownCtId, 1));
    assertTrue(ex.getMessage().contains(unknownCtId));
  }

  @Test
  void shouldIgnoreInsertWhenNoCardTerminalsAreConfigured() {
    val sca = SmartcardArchive.fromResources();

    val operator = new CardTerminalOperator(List.of());
    val egk = sca.getEgk(0);
    assertDoesNotThrow(() -> operator.insertCard(egk));
    assertDoesNotThrow(() -> operator.insertCard(egk, "ct-1", 1));
  }

  @Test
  void shouldIgnoreInsertWhenCardAlreadyInserted() {
    val sca = SmartcardArchive.fromResources();

    val egk = sca.getEgk(0);
    val slot = new CardTerminalSlot(1);
    slot.setIccsn(egk.getIccsn());

    val cardTerminal = mock(CardTerminal.class);
    when(cardTerminal.getCtId()).thenReturn("ct-1");
    when(cardTerminal.getAllSlots()).thenReturn(List.of(slot));

    val operator = new CardTerminalOperator(List.of(cardTerminal));

    assertDoesNotThrow(() -> operator.insertCard(egk));
    verify(cardTerminal, never()).insertCard(egk);

    assertDoesNotThrow(() -> operator.insertCard(egk, "ct-1", 1));
    verify(cardTerminal, never()).insertCard(egk, 1);

    // getCtId() must have been called at least once so the terminal ID is included in the log
    verify(cardTerminal, atLeastOnce()).getCtId();
  }

  @Test
  void shouldLogTerminalIdWhenCardIsAlreadyInsertedInSpecificTerminal() {
    val sca = SmartcardArchive.fromResources();

    val egk = sca.getEgk(0);
    val occupiedSlot = new CardTerminalSlot(1);
    occupiedSlot.setIccsn(egk.getIccsn());

    // card is in ct-2, not ct-1
    val ct1 = mock(CardTerminal.class);
    when(ct1.getCtId()).thenReturn("ct-1");
    when(ct1.getAllSlots()).thenReturn(List.of(new CardTerminalSlot(1)));

    val ct2 = mock(CardTerminal.class);
    when(ct2.getCtId()).thenReturn("ct-2");
    when(ct2.getAllSlots()).thenReturn(List.of(occupiedSlot));

    val operator = new CardTerminalOperator(List.of(ct1, ct2));

    assertDoesNotThrow(() -> operator.insertCard(egk));
    // neither terminal should receive an insertCard call because the card is already present
    verify(ct1, never()).insertCard(egk);
    verify(ct2, never()).insertCard(egk);

    // only ct-2 holds the card, so only ct-2's ID should be retrieved for the log message
    verify(ct2, atLeastOnce()).getCtId();
  }
}
