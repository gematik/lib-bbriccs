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

import de.gematik.bbriccs.smartcards.Smartcard;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;

class CardTerminalTest {

  @Test
  void shouldHaveDefaultImplementations() {
    val ct = new TestCardTerminal();
    assertDoesNotThrow(ct::connect);
    assertFalse(ct.hasFreeSlot());
    assertDoesNotThrow(ct::disconnect);
  }

  private static class TestCardTerminal implements CardTerminal {

    @Override
    public String getCtId() {
      return null;
    }

    @Override
    public void insertCard(Smartcard card, int slotId) {}

    @Override
    public void insertCard(Smartcard card) {}

    @Override
    public void resetSlots() {}

    @Override
    public Optional<CardTerminalSlot> getFreeSlot() {
      return Optional.empty();
    }
  }
}
