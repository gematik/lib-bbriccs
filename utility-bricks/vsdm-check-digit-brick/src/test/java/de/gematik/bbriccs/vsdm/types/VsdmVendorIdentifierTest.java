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
import de.gematik.bbriccs.vsdm.exceptions.ParsingException;
import java.util.Base64;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VsdmVendorIdentifierTest {

  private static final String CHECK_DIGIT_VERSION_V1 =
      "WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjkJuPVMWhLD6OBwggI=";
  private static final String CHECK_DIGIT_VERSION_V2 =
      "3gyWVfvt1Yncz80adEC997AOEMJAzBxElpKwgyPfL+mGjrG31Yo4AqT9vT168v0=";

  @Test
  void shouldParseValidDataForVersion1() {
    byte[] data = Base64.getDecoder().decode(CHECK_DIGIT_VERSION_V1);
    VsdmVendorIdentifier result = VsdmVendorIdentifier.parseV1(data);
    assertEquals('C', result.identifier());
  }

  @Test
  void shouldParseValidDataForVersion2() {
    byte[] data = Base64.getDecoder().decode(CHECK_DIGIT_VERSION_V2);
    VsdmCheckDigitVersion version = VsdmCheckDigitVersion.V2;
    VsdmVendorIdentifier result =
        VsdmVendorIdentifier.parseV2(data, new VsdmKeyVersion('2', version));
    assertEquals('X', result.identifier());
  }

  @Test
  void shouldThrowParsingExceptionForInvalidData() {
    byte[] data = {
      /* invalid data */
    };
    assertThrows(ParsingException.class, () -> VsdmVendorIdentifier.parseV1(data));
  }

  @Test
  void shouldThrowParsingExceptionForEmptyData() {
    byte[] data = {};
    assertThrows(ParsingException.class, () -> VsdmVendorIdentifier.parseV1(data));
  }

  @Test
  void shouldThrowParsingExceptionForNullData() {
    byte[] data = null;
    assertThrows(NullPointerException.class, () -> VsdmVendorIdentifier.parseV1(data));
  }

  @Test
  void shouldThrowParsingExceptionForDataWithNonAsciiCharacters() {
    byte[] data = {(byte) 0xC3, (byte) 0xA9}; // é in UTF-8
    assertThrows(ParsingException.class, () -> VsdmVendorIdentifier.parseV1(data));
  }

  @ParameterizedTest
  @ValueSource(bytes = {(byte) '@', (byte) 'a', (byte) ' ', (byte) 0x01})
  void shouldThrowParsingExceptionForDataWithSpecialCharacters(byte value) {
    byte[] data = {value}; // '@' is not a valid identifier
    assertThrows(ParsingException.class, () -> VsdmVendorIdentifier.parseV1(data));
  }

  @Test
  void shouldReturnHashCode() {
    val identifier = new VsdmVendorIdentifier('A', VsdmCheckDigitVersion.V1);
    assertDoesNotThrow(identifier::hashCode);
  }

  @ParameterizedTest
  @ValueSource(chars = {'A', ')', '1'})
  void shouldHandleDifferentIdentifier(char value) {
    val data = new byte[] {(byte) value};
    assertDoesNotThrow(
        () ->
            VsdmVendorIdentifier.parseV2(data, new VsdmKeyVersion('I', VsdmCheckDigitVersion.V2)));
  }
}
