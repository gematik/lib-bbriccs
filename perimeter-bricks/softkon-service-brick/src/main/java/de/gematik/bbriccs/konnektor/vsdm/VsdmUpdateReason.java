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

package de.gematik.bbriccs.konnektor.vsdm;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.konnektor.exceptions.ParsingUpdateReasonException;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum VsdmUpdateReason {
  UFS_UPDATE('U', "Update Flag Service (UFS) Anfrage"),
  VSD_UPDATE('V', "Versichertenstammdaten (VSD) Update"),
  CARD_MANAGEMENT_UPDATE('C', "Kartenmanagement (CMS) Update"),
  INVALID('I', "Invalid Reason (Test purpose)");

  private final String description;
  private final char identifier;

  VsdmUpdateReason(char identifier, String description) {
    this.identifier = identifier;
    this.description = description;
  }

  public static VsdmUpdateReason fromChecksum(char value) throws ParsingUpdateReasonException {
    return Arrays.stream(VsdmUpdateReason.values())
        .filter(reason -> reason.getIdentifier() == value)
        .findFirst()
        .orElseThrow(() -> new ParsingUpdateReasonException(value));
  }

  @Override
  public String toString() {
    return format("Identifier {0} Description: {1}", this.identifier, this.description);
  }
}