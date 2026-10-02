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

import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalNotFoundException;
import de.gematik.bbriccs.cardterminal.exceptions.NoFreeSlotException;
import de.gematik.bbriccs.smartcards.Smartcard;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public record CardTerminalOperator(Set<CardTerminal> cardTerminals) {

  public CardTerminalOperator(Collection<CardTerminal> cardTerminals) {
    this(Collections.unmodifiableSet(new LinkedHashSet<>(cardTerminals)));
  }

  public Set<CardTerminal> getCardTerminals() {
    return this.cardTerminals;
  }

  public boolean hasFreeSlot() {
    return this.cardTerminals.stream().anyMatch(CardTerminal::hasFreeSlot);
  }

  public void insertCard(Smartcard card) {
    if (cardTerminals().isEmpty()) {
      return;
    }
    if (isCardAlreadyInserted(card)) {
      return;
    }
    val ct =
        this.cardTerminals.stream()
            .filter(CardTerminal::hasFreeSlot)
            .findFirst()
            .orElseThrow(() -> new NoFreeSlotException(card));
    ct.insertCard(card);
  }

  public void insertCard(Smartcard card, String cardTerminalId, int slot) {
    if (isCardAlreadyInserted(card)) {
      return;
    }
    if (cardTerminals().isEmpty()) {
      return;
    }
    val ct =
        this.cardTerminals.stream()
            .filter(it -> it.getCtId().equals(cardTerminalId))
            .findFirst()
            .orElseThrow(() -> new CardTerminalNotFoundException(cardTerminalId));
    ct.insertCard(card, slot);
  }

  private boolean isCardAlreadyInserted(Smartcard card) {
    // Find the terminal that already holds the card so we can include it in the log message
    val terminalWithCard =
        this.cardTerminals.stream()
            .filter(
                ct -> {
                  val slots = ct.getAllSlots();
                  return slots != null
                      && slots.stream()
                          .map(CardTerminalSlot::getIccsn)
                          .flatMap(Optional::stream)
                          .anyMatch(card.getIccsn()::equals);
                })
            .findFirst();

    terminalWithCard.ifPresent(
        ct ->
            log.debug(
                "smartcard {} is already inserted in card terminal {}",
                card.getIccsn(),
                ct.getCtId()));

    return terminalWithCard.isPresent();
  }
}
