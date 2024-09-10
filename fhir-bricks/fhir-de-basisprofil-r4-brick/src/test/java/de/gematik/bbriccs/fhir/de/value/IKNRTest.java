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

package de.gematik.bbriccs.fhir.de.value;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class IKNRTest {

  @ParameterizedTest(name = "[{index}]: IKNR {0} is valid")
  @ValueSource(strings = {"260326822"})
  void shouldValidateIKNR(String value) {
    val sidIknr = IKNR.asSidIknr(value);
    val argeIknr = IKNR.asArgeIknr(value);
    assertTrue(sidIknr.isValid());
    assertTrue(argeIknr.isValid());
  }

  @RepeatedTest(5)
  void shouldGenerateRandomValidIKNR() {
    val iknr = IKNR.random();
    assertTrue(iknr.isValid());
  }

  @Test
  void shouldNotEqualOnDifferentSystems() {
    val first = IKNR.asArgeIknr("413799875");
    val second = IKNR.asSidIknr("413799875");
    assertNotEquals(first, second);
  }

  @ParameterizedTest(name = "[{index}]: IKNR {0} is invalid")
  @ValueSource(strings = {"413799876", "abc", "12345678", "1234567890", "", " "})
  @NullSource
  void shouldCheckInvalidIKNRCheckDigit(String value) {
    val argeIknr = IKNR.asArgeIknr(value);
    val sidIknr = IKNR.asSidIknr(value);
    assertFalse(argeIknr.isValid());
    assertFalse(sidIknr.isValid());
  }
}
