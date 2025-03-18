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

package de.gematik.bbriccs.fhir.validation.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class CodeSystemFilterTest {

  @Test
  void shouldNotSupportNullCodeSystems() {
    val filter = new CodeSystemFilter(FhirContext.forR4(), List.of());
    val vsc = mock(ValidationSupportContext.class);
    assertFalse(filter.isCodeSystemSupported(vsc, null));
  }

  @Test
  void shouldNotValidateNullCodeSystem() {
    val filter = new CodeSystemFilter(FhirContext.forR4(), List.of());
    val vsc = mock(ValidationSupportContext.class);
    val cvo = mock(ConceptValidationOptions.class);
    val result = filter.validateCode(vsc, cvo, null, "CODE", "display", "https://valueset.com");
    assertNull(result);
  }
}
