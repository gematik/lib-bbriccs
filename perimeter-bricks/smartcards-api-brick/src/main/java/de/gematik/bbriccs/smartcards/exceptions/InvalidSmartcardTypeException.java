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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.SmartcardType;

public class InvalidSmartcardTypeException extends RuntimeException {

  public InvalidSmartcardTypeException(SmartcardType type) {
    this(type.name());
  }

  public InvalidSmartcardTypeException(String type) {
    super(format("Smartcard Type {0} is invalid", type));
  }

  public InvalidSmartcardTypeException(SmartcardType typeGiven, SmartcardType typeExpected) {
    super(format("Smartcard Type {0} was given, but configured {1}", typeGiven, typeExpected));
  }
}
