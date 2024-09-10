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

package de.gematik.bbriccs.fhir.de.valueset;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ActCodeTest {

  @ParameterizedTest
  @EnumSource(value = ActCode.class)
  void shouldHaveDisplayAndDefinition(ActCode cs) {
    val code = ActCode.fromCode(cs.getCode());
    assertEquals(cs, code);
    assertNotNull(code.getDisplay());
    assertNotNull(code.getDefinition());
    assertEquals(HL7CodeSystem.ACT_CODE, code.getCodeSystem());
  }
}
