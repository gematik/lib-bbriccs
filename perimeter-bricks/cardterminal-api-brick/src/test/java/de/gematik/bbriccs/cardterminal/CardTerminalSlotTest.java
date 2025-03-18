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

package de.gematik.bbriccs.cardterminal;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class CardTerminalSlotTest {

  @Test
  void shouldBeFreeAfterCreation() {
    val slot = new CardTerminalSlot(0);
    assertTrue(slot.isFree());
  }

  @Test
  void shouldNotFailOnRemoveFromFreeSlot() {
    val slot = new CardTerminalSlot(0);
    assertDoesNotThrow(slot::remove);
    assertTrue(slot.isFree());
  }

  @Test
  void shouldInsert() {
    val slot = new CardTerminalSlot(0);
    assertDoesNotThrow(() -> slot.inserte("iccsn"));
    assertFalse(slot.isFree());
    assertTrue(slot.isOccupied());
  }
}
