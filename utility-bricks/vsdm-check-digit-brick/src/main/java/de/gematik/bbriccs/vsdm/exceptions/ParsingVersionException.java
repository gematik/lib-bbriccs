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

package de.gematik.bbriccs.vsdm.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.types.VsdmKeyVersion;

public class ParsingVersionException extends RuntimeException {
  public ParsingVersionException(
      VsdmCheckDigitVersion expectedVersion, VsdmCheckDigitVersion actualVersion) {
    super(
        format(
            "The checksum should contain version ''{0}'', but it contains ''{1}''.",
            expectedVersion, actualVersion));
  }

  public ParsingVersionException(VsdmKeyVersion expectedVersion, VsdmKeyVersion actualVersion) {
    super(
        format(
            "Check digit should be encrypted with key version ''{0}''. Instead, key version ''{1}''"
                + " was used",
            expectedVersion.keyVersion(), actualVersion.keyVersion()));
  }
}
