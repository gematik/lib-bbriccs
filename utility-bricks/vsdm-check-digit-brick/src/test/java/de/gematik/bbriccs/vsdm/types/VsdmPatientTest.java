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

import de.gematik.bbriccs.vsdm.VsdmUtils;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VsdmPatientTest {
  @ParameterizedTest
  @MethodSource("shouldGenerateHashSuccessfulTestdata")
  void shouldGenerateHashSuccessful(
      LocalDate insuranceStartDate, String street, String expectedHash) {

    assertEquals(
        expectedHash,
        VsdmUtils.bytesToHex(VsdmPatient.generateHash(insuranceStartDate, street))
            .toLowerCase()
            .replace(" ", ""));
  }

  @Test
  void shouldThrowExceptions() {
    assertThrows(NullPointerException.class, () -> VsdmPatient.generateHash(null, null));
    val date = LocalDate.now();
    assertThrows(NullPointerException.class, () -> VsdmPatient.generateHash(date, null));
    assertThrows(NullPointerException.class, () -> VsdmPatient.generateHash(null, ""));
  }

  @Test
  void testCreateVsdmPatient() {
    assertDoesNotThrow(() -> new VsdmPatient(VsdmKvnr.from("A123456789"), LocalDate.now()));
    val patient = new VsdmPatient(VsdmKvnr.from("A123456789"), LocalDate.now());
    assertEquals("", patient.getStreet());
    assertNotNull(patient.getInsuranceStartDate());
  }

  /**
   * Examples form A_27352
   *
   * @return a argument stream of a local date, a street and the expected hash
   */
  public Stream<Arguments> shouldGenerateHashSuccessfulTestdata() {
    return Stream.of(
        Arguments.of(LocalDate.parse("2019-02-12"), "", "4885ee8394"),
        Arguments.of(LocalDate.parse("1998-11-23"), "Berliner Straße", "6545491d14"),
        Arguments.of(LocalDate.parse("1984-10-03"), "Angermünder Straße", "7cc49e7af4"),
        Arguments.of(LocalDate.parse("2001-01-19"), "Björnsonstraße", "186269e4f7"),
        Arguments.of(LocalDate.parse("2004-07-18"), "Schönhauser Allee", "353646b5c8"));
  }
}
