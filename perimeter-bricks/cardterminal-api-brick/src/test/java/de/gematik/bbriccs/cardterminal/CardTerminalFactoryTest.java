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

import de.gematik.bbriccs.cardterminal.cfg.CardTerminalConfiguration;
import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalException;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class CardTerminalFactoryTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(CardTerminalFactory.class));
  }

  @Test
  void shouldThrowOnUnknownCardTerminalType() {
    val cfg = new TestCardTerminalConfiguration();
    cfg.setType("TestCT");
    assertThrows(CardTerminalException.class, () -> CardTerminalFactory.create(cfg));
  }

  private static class TestCardTerminalConfiguration extends CardTerminalConfiguration {}
}
