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

package de.gematik.bbriccs.fhir.de.valueset;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InsuranceTypeDeTest {

  @ParameterizedTest
  @EnumSource(value = InsuranceTypeDe.class)
  void shouldHaveDisplay(InsuranceTypeDe insuranceTypeDe) {
    val code = InsuranceTypeDe.fromCode(insuranceTypeDe.getCode());
    assertEquals(insuranceTypeDe, code);
    assertNotNull(code.getDisplay());
    assertEquals(DeBasisProfilCodeSystem.VERSICHERUNGSART_DE_BASIS, code.getCodeSystem());
  }

  @ParameterizedTest
  @EnumSource(value = InsuranceTypeDe.class)
  void shouldCreateFromCoding(InsuranceTypeDe insuranceTypeDe) {
    val fromCode = InsuranceTypeDe.fromCode(insuranceTypeDe.asCoding());
    assertEquals(insuranceTypeDe, fromCode);
    assertNotNull(fromCode.getDisplay());
    assertEquals(DeBasisProfilCodeSystem.VERSICHERUNGSART_DE_BASIS, fromCode.getCodeSystem());
  }
}
