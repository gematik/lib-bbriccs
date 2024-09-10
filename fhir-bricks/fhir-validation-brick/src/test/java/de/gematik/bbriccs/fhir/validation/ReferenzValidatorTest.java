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

package de.gematik.bbriccs.fhir.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.commons.exceptions.ValidationModuleInitializationException;
import lombok.val;
import org.junit.jupiter.api.Test;

class ReferenzValidatorTest {

  @Test
  void shouldThrowOnInvalidConfiguration() {
    val svm = mock(SupportedValidationModule.class);
    assertThrows(
        ValidationModuleInitializationException.class,
        () -> ReferenzValidator.withValidationModule(svm));
  }
}
