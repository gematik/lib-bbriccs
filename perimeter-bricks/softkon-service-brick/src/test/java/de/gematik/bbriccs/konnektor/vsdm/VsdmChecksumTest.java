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

package de.gematik.bbriccs.konnektor.vsdm;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.konnektor.exceptions.InvalidKeyLengthException;
import de.gematik.bbriccs.konnektor.exceptions.ParsingUpdateReasonException;
import lombok.val;
import org.junit.jupiter.api.Test;

class VsdmChecksumTest {

  private static final String VALID_CHECKSUM =
      "VDA0NjE0MjQ5OTE3MTAyNzY1NTJVVDEufU90p/1XligLKMG5VLlJrzQCyUh+JSU=";

  @Test
  void shouldThrowOnInvalidChecksum() {
    assertThrows(ParsingUpdateReasonException.class, () -> VsdmChecksum.parse("invalid"));
  }

  @Test
  void shouldParseValidChecksum() {
    val checksum = assertDoesNotThrow(() -> VsdmChecksum.parse(VALID_CHECKSUM));
    assertNotNull(checksum);
    assertEquals('1', checksum.getVersion());
    assertEquals('T', checksum.getIdentifier());
    assertEquals("T046142499", checksum.getKvnr());
    assertEquals(VsdmUpdateReason.UFS_UPDATE, checksum.getUpdateReason());
    assertDoesNotThrow(checksum::toString);
  }

  @Test
  void shouldThrowOnSigningWithInvalidKeyLength() {
    val checksum = assertDoesNotThrow(() -> VsdmChecksum.parse(VALID_CHECKSUM));
    val data = "hello".getBytes();
    assertThrows(InvalidKeyLengthException.class, () -> checksum.sign(data));
  }

  @Test
  void shouldThrowOnTooShortChecksum() {
    val checkSum = VALID_CHECKSUM.substring(0, 10);
    assertThrows(ParsingUpdateReasonException.class, () -> VsdmChecksum.parse(checkSum));
  }
}
