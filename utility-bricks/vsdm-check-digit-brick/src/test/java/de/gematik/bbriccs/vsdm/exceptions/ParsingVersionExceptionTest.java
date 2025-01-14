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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.types.VsdmKeyVersion;
import org.junit.jupiter.api.Test;

class ParsingVersionExceptionTest {
  @Test
  void shouldCreateParsingVersionExceptionWithCheckDigitVersion() {
    VsdmCheckDigitVersion expectedVersion = VsdmCheckDigitVersion.V1;
    VsdmCheckDigitVersion actualVersion = VsdmCheckDigitVersion.V2;
    ParsingVersionException exception = new ParsingVersionException(expectedVersion, actualVersion);
    assertNotNull(exception);
    assertEquals(
        "The checksum should contain version 'V1', but it contains 'V2'.", exception.getMessage());
  }

  @Test
  void shouldCreateParsingVersionExceptionWithKeyVersion() {
    VsdmKeyVersion expectedVersion = new VsdmKeyVersion('1', VsdmCheckDigitVersion.V1);
    VsdmKeyVersion actualVersion = new VsdmKeyVersion('2', VsdmCheckDigitVersion.V1);
    ParsingVersionException exception = new ParsingVersionException(expectedVersion, actualVersion);
    assertNotNull(exception);
    assertEquals(
        "Check digit should be encrypted with key version '1'. Instead, key version '2' was used",
        exception.getMessage());
  }
}
