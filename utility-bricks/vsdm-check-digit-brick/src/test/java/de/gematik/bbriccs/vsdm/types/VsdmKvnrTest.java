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

package de.gematik.bbriccs.vsdm.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.exceptions.ParsingException;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class VsdmKvnrTest {
  private static final String CHECK_DIGIT_DECRYPTED_V2 = "EL5l82UB9gJBMTIzNDU2Nzg5";

  @Test
  void shouldGenerateCorrectByteArray() {
    VsdmKvnr vsdmKvnr = new VsdmKvnr("1234567890");
    byte[] result = vsdmKvnr.generate();
    assertArrayEquals("1234567890".getBytes(), result);
  }

  @Test
  void shouldParseCorrectKvnrForV1() {
    byte[] data = "1234567890".getBytes();
    VsdmKvnr vsdmKvnr = VsdmKvnr.parse(data, VsdmCheckDigitVersion.V1);
    assertEquals("1234567890", vsdmKvnr.kvnr());
  }

  @Test
  void shouldParseCorrectKvnrForV2() {
    byte[] data = Base64.getDecoder().decode(CHECK_DIGIT_DECRYPTED_V2);
    VsdmKvnr vsdmKvnr = VsdmKvnr.parse(data, VsdmCheckDigitVersion.V2);
    assertEquals("A123456789", vsdmKvnr.kvnr());
    assertEquals("A123456789", vsdmKvnr.toString());
  }

  @Test
  void shouldThrowParsingExceptionForInvalidDataV1() {
    byte[] data = new byte[1];
    assertThrows(ParsingException.class, () -> VsdmKvnr.parse(data, VsdmCheckDigitVersion.V1));
  }

  @Test
  void shouldThrowParsingExceptionForInvalidDataV2() {
    byte[] data = new byte[5];
    assertThrows(ParsingException.class, () -> VsdmKvnr.parse(data, VsdmCheckDigitVersion.V2));
  }

  @Test
  void shouldReturnTrueForEqualKvnrObjects() {
    VsdmKvnr vsdmKvnr1 = new VsdmKvnr("1234567890");
    VsdmKvnr vsdmKvnr2 = new VsdmKvnr("1234567890");
    assertEquals(vsdmKvnr1, vsdmKvnr2);
  }

  @Test
  void shouldReturnFalseForNonEqualKvnrObjects() {
    VsdmKvnr vsdmKvnr1 = new VsdmKvnr("1234567890");
    VsdmKvnr vsdmKvnr2 = new VsdmKvnr("0987654321");
    assertNotEquals(vsdmKvnr1, vsdmKvnr2);
  }

  @Test
  void shouldReturnFalseForNullComparison() {
    VsdmKvnr vsdmKvnr = new VsdmKvnr("1234567890");
    assertNotEquals(null, vsdmKvnr);
  }

  @Test
  void shouldReturnSameHashCodeForEqualKvnrObjects() {
    VsdmKvnr vsdmKvnr1 = new VsdmKvnr("1234567890");
    VsdmKvnr vsdmKvnr2 = new VsdmKvnr("1234567890");
    assertEquals(vsdmKvnr1.hashCode(), vsdmKvnr2.hashCode());
  }

  @Test
  void shouldReturnDifferentHashCodeForNonEqualKvnrObjects() {
    VsdmKvnr vsdmKvnr1 = new VsdmKvnr("1234567890");
    VsdmKvnr vsdmKvnr2 = new VsdmKvnr("0987654321");
    assertNotEquals(vsdmKvnr1.hashCode(), vsdmKvnr2.hashCode());
  }
}
