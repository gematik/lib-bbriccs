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

package de.gematik.bbriccs.smartcards.exceptions;

import static java.text.MessageFormat.*;

import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.bbriccs.smartcards.SmartcardType;

public class CardNotFoundException extends RuntimeException {

  public CardNotFoundException(String iccsn) {
    super(format("Smartcard with ICCSN {0} not found", iccsn));
  }

  public CardNotFoundException(SmartcardType type, String iccsn) {
    super(format("Card of type {0} with ICCSN/KVNR {1} not found", type, iccsn));
  }

  public <T extends Smartcard> CardNotFoundException(Class<T> type, String iccsn) {
    super(format("Card of type {0} with ICCSN/KVNR {1} not found", type.getSimpleName(), iccsn));
  }

  public CardNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
