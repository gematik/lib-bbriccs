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

package de.gematik.bbriccs.fhir.de.value;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PZNTest {

  @ParameterizedTest(name = "[{index}]: PZN {0} is valid")
  @ValueSource(strings = {"27580899", "20987327", "91168337"})
  void shouldValidPZN(String value) {
    val pzn = PZN.from(value);
    assertTrue(pzn.isValid());
  }

  @RepeatedTest(5)
  void shouldGenerateRandomValidPZN() {
    val pzn = PZN.random();
    assertTrue(pzn.isValid());
  }

  @ParameterizedTest(name = "[{index}]: PZN {0} is invalid")
  @ValueSource(strings = {"abc", "1234", "123456789", "91168338"})
  @NullSource
  void shouldCheckInvalidPznCheckDigit(String value) {
    val pzn = PZN.from(value);
    assertFalse(pzn.isValid());
  }

  @ParameterizedTest(name = "[{index}]: PZN {0} is invalid with CheckSum 10")
  @ValueSource(strings = {"06659310", "53089840", "30650630"})
  @NullSource
  void shouldCheckInvalidPznCheckDigitTen(String value) {
    val pzn = PZN.from(value);
    assertFalse(pzn.isValid());
  }

  @Test
  void shouldBuildAsNamedCodeable() {
    val pzn = PZN.from("27580899");
    val codeable = pzn.asNamedCodeable();

    assertNotNull(codeable.getText());
    val coding = codeable.getCodingFirstRep();
    assertEquals("27580899", coding.getCode());
  }

  @Test
  void shouldGeneratePznFromCoding() {
    val coding = DeBasisProfilCodeSystem.PZN.asCoding("27580899");
    val pzn = PZN.from(coding);
    assertEquals("27580899", pzn.getValue());
  }
}
