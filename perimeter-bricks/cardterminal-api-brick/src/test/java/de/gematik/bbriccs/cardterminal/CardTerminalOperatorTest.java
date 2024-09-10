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

package de.gematik.bbriccs.cardterminal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.cardterminal.exceptions.NoFreeSlotException;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class CardTerminalOperatorTest {

  @Test
  void shouldInsertSmartcard() {
    val sca = SmartcardArchive.fromResources();

    val cardTerminal = mock(CardTerminal.class);
    when(cardTerminal.hasFreeSlot()).thenReturn(true);

    val egk = sca.getEgk(0);
    val operator = new CardTerminalOperator(List.of(cardTerminal));
    assertDoesNotThrow(() -> operator.insertCard(egk));
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
  void shouldThrowOnNoFreeSlotsBecauseOfNoCardTerminals() {
    val sca = SmartcardArchive.fromResources();

    val operator = new CardTerminalOperator(List.of());
    val egk = sca.getEgk(0);
    assertThrows(NoFreeSlotException.class, () -> operator.insertCard(egk));
  }
}
