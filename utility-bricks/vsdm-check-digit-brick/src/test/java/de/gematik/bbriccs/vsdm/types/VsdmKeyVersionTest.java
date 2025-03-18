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

package de.gematik.bbriccs.vsdm.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import org.junit.jupiter.api.Test;

class VsdmKeyVersionTest {
  @Test
  void shouldGenerateCorrectByteForV1Version() {
    VsdmKeyVersion vsdmKeyVersion = new VsdmKeyVersion('A', VsdmCheckDigitVersion.V1);
    byte result = vsdmKeyVersion.generate();
    assertEquals((byte) 'A', result);
  }

  @Test
  void shouldGenerateCorrectByteForV2Version() {
    VsdmKeyVersion vsdmKeyVersion = new VsdmKeyVersion('A', VsdmCheckDigitVersion.V2);
    byte result = vsdmKeyVersion.generate();
    assertEquals((byte) ('A' - '0'), result);
  }

  @Test
  void shouldReturnCorrectStringRepresentation() {
    VsdmKeyVersion vsdmKeyVersion = new VsdmKeyVersion('A', VsdmCheckDigitVersion.V1);
    assertEquals("A", vsdmKeyVersion.toString());
  }
}
