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

package de.gematik.bbriccs.fhir.de.valueset;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CountryTest {

  @ParameterizedTest
  @EnumSource(
      value = Country.class,
      names = {"A", "B", "C", "D"})
  void shouldHaveDisplay(Country country) {
    val country1 = Country.fromCode(country.getCode());
    assertEquals(country, country1);
    assertNotNull(country1.getDisplay());
    assertEquals(DeBasisProfilCodeSystem.LAENDERKENNZEICHEN, country1.getCodeSystem());
  }
}
