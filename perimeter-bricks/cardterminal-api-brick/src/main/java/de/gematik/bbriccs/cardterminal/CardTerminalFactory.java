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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.cardterminal.cfg.CardTerminalConfiguration;
import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalException;
import java.util.ServiceLoader;
import lombok.val;

public class CardTerminalFactory {

  private CardTerminalFactory() {
    throw new IllegalAccessError("Utility class: don't use the constructor");
  }

  public static CardTerminal create(CardTerminalConfiguration cfg) {
    val ctServiceFactory = loadCardTerminalFactoryService(cfg.getType());
    return ctServiceFactory.buildCardTerminalClient(cfg).connect();
  }

  private static CardTerminalFactoryService loadCardTerminalFactoryService(String named) {
    val loader = ServiceLoader.load(CardTerminalFactoryService.class);
    return loader.stream()
        .map(ServiceLoader.Provider::get)
        .filter(ctsf -> ctsf.getType().equalsIgnoreCase(named))
        .findFirst()
        .orElseThrow(
            () ->
                new CardTerminalException(
                    format("No CardTerminalFactoryService found for {0}", named)));
  }
}
