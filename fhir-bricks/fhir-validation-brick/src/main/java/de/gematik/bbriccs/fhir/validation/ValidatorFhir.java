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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;

public interface ValidatorFhir {

  /**
   * Get the {@link FhirContext} used by this validator
   *
   * @return the {@link FhirContext} used for validation
   */
  FhirContext getContext();

  /**
   * Perform a validation on the given string content
   *
   * @param content to be validated
   * @return the {@link ValidationResult}
   */
  ValidationResult validate(String content);

  /**
   * Perform a validation on the given content and extract the overall result from the {@link
   * ValidationResult}
   *
   * @param content to be validated
   * @return true if the {@link ValidationResult} was successful or false otherwise
   */
  default boolean isValid(String content) {
    return this.validate(content).isSuccessful();
  }
}
