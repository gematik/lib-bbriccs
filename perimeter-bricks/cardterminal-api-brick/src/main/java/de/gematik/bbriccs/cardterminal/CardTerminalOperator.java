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

import de.gematik.bbriccs.cardterminal.exceptions.NoFreeSlotException;
import de.gematik.bbriccs.smartcards.Smartcard;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CardTerminalOperator {

  private final Set<CardTerminal> cardTerminals;

  public CardTerminalOperator(Collection<CardTerminal> cardTerminals) {
    this.cardTerminals = new HashSet<>(cardTerminals);
  }

  public void insertCard(Smartcard card) {
    val cardTerminal =
        this.cardTerminals.stream()
            .filter(CardTerminal::hasFreeSlot)
            .findFirst()
            .orElseThrow(() -> new NoFreeSlotException(card));
    cardTerminal.insertCard(card);
  }
}
