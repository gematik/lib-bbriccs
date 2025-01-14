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
import de.gematik.bbriccs.vsdm.exceptions.ParsingUpdateResonException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VsdmUpdateReasonTest {

  @Test
  void shouldThrowParsingUpdateResonExceptionForInvalidIdentifier() {
    char invalidIdentifier = 'X';
    assertThrows(
        ParsingUpdateResonException.class, () -> VsdmUpdateReason.fromChecksum(invalidIdentifier));
  }

  @Test
  void shouldThrowParsingExceptionForInvalidDataV1() {
    byte[] data = new byte[1];
    assertThrows(ParsingException.class, () -> VsdmKvnr.parse(data, VsdmCheckDigitVersion.V1));
  }

  @Test
  void shouldThrowParsingExceptionForInvalidDataV2() {
    byte[] data = new byte[1];
    assertThrows(ParsingException.class, () -> VsdmKvnr.parse(data, VsdmCheckDigitVersion.V2));
  }

  @Test
  void shouldReturnCorrectDescriptionForUfsUpdate() {
    VsdmUpdateReason reason = VsdmUpdateReason.UFS_UPDATE;
    assertEquals("Update Flag Service (UFS) Anfrage", reason.getDescription());
  }

  @Test
  void shouldReturnCorrectIdentifierForVsdUpdate() {
    VsdmUpdateReason reason = VsdmUpdateReason.VSD_UPDATE;
    assertEquals('V', reason.getIdentifier());
  }

  @Test
  void shouldGenerateCorrectByteForCardManagementUpdate() {
    VsdmUpdateReason reason = VsdmUpdateReason.CARD_MANAGEMENT_UPDATE;
    assertEquals((byte) 'C', reason.generate());
  }

  @Test
  void shouldReturnCorrectStringRepresentationForInvalidReason() {
    VsdmUpdateReason reason = VsdmUpdateReason.INVALID;
    assertEquals("Identifier I Description: Invalid Reason (Test purpose)", reason.toString());
  }

  static Stream<Arguments> shouldReturnCorrectUpdateReasonFromChecksum() {
    return Stream.of(
        Arguments.of('U', VsdmUpdateReason.UFS_UPDATE),
        Arguments.of('V', VsdmUpdateReason.VSD_UPDATE),
        Arguments.of('C', VsdmUpdateReason.CARD_MANAGEMENT_UPDATE));
  }

  @ParameterizedTest
  @MethodSource("shouldReturnCorrectUpdateReasonFromChecksum")
  void shouldReturnCorrectUpdateReasonFromChecksum(char identifier, VsdmUpdateReason expectedReason)
      throws ParsingUpdateResonException {
    VsdmUpdateReason reason = VsdmUpdateReason.fromChecksum(identifier);
    assertEquals(expectedReason, reason);
  }

  @Test
  void shouldThrowParsingUpdateResonExceptionForUnknownIdentifier() {
    char unknownIdentifier = 'Z';
    assertThrows(
        ParsingUpdateResonException.class, () -> VsdmUpdateReason.fromChecksum(unknownIdentifier));
  }
}
