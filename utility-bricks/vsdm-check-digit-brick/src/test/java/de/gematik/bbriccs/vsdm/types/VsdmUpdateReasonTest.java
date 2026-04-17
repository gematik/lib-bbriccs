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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.bbriccs.vsdm.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.exceptions.ParsingUpdateReasonException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class VsdmUpdateReasonTest {
  static Stream<Arguments> shouldResolveValidIdentifiers() {
    return Stream.of(
        Arguments.of('U', VsdmUpdateReason.UFS_UPDATE),
        Arguments.of('V', VsdmUpdateReason.VSD_UPDATE),
        Arguments.of('C', VsdmUpdateReason.CARD_MANAGEMENT_UPDATE));
  }

  @ParameterizedTest
  @MethodSource("shouldResolveValidIdentifiers")
  void shouldResolveValidIdentifiers(char identifier, VsdmUpdateReason expected)
      throws ParsingUpdateReasonException {
    assertEquals(expected, VsdmUpdateReason.fromChecksum(identifier));
  }

  @ParameterizedTest
  @ValueSource(chars = {'X', 'Z', 'I', '0', ' '})
  void shouldThrowForUnknownIdentifier(char unknown) {
    assertThrows(ParsingUpdateReasonException.class, () -> VsdmUpdateReason.fromChecksum(unknown));
  }

  @ParameterizedTest
  @EnumSource(VsdmUpdateReason.class)
  void shouldGenerateByteMatchingIdentifier(VsdmUpdateReason reason) {
    assertEquals((byte) reason.getIdentifier(), reason.generate());
  }

  @ParameterizedTest
  @EnumSource(VsdmUpdateReason.class)
  void shouldContainIdentifierAndDescriptionInToString(VsdmUpdateReason reason) {
    String s = reason.toString();
    assertTrue(s.contains(String.valueOf(reason.getIdentifier())));
    assertTrue(s.contains(reason.getDescription()));
  }

  @Test
  void shouldHaveCorrectIdentifierForUfsUpdate() {
    assertEquals('U', VsdmUpdateReason.UFS_UPDATE.getIdentifier());
  }

  @Test
  void shouldHaveCorrectDescriptionForUfsUpdate() {
    assertEquals("Update Flag Service (UFS) Anfrage", VsdmUpdateReason.UFS_UPDATE.getDescription());
  }

  @Test
  void shouldHaveCorrectIdentifierForVsdUpdate() {
    assertEquals('V', VsdmUpdateReason.VSD_UPDATE.getIdentifier());
  }

  @Test
  void shouldHaveCorrectDescriptionForVsdUpdate() {
    assertEquals(
        "Versichertenstammdaten (VSD) Update", VsdmUpdateReason.VSD_UPDATE.getDescription());
  }

  @Test
  void shouldHaveCorrectIdentifierForCardManagementUpdate() {
    assertEquals('C', VsdmUpdateReason.CARD_MANAGEMENT_UPDATE.getIdentifier());
  }

  @Test
  void shouldHaveCorrectDescriptionForCardManagementUpdate() {
    assertEquals(
        "Kartenmanagement (CMS) Update", VsdmUpdateReason.CARD_MANAGEMENT_UPDATE.getDescription());
  }

  @Test
  void shouldHaveCorrectIdentifierForInvalid() {
    assertEquals('I', VsdmUpdateReason.INVALID.getIdentifier());
  }

  @Test
  void shouldHaveCorrectDescriptionForInvalid() {
    assertEquals("Invalid Reason (Test purpose)", VsdmUpdateReason.INVALID.getDescription());
  }

  @Test
  void shouldProduceExpectedToStringForInvalid() {
    assertEquals(
        "Identifier I Description: Invalid Reason (Test purpose)",
        VsdmUpdateReason.INVALID.toString());
  }
}
