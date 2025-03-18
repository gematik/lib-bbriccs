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

package de.gematik.bbriccs.fhir.validation;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public abstract class ValidatorFhirBase implements ValidatorFhir {

  protected ValidationResult validateSafely(Supplier<ValidationResult> validationResultSupplier) {
    try {
      return validationResultSupplier.get();
    } catch (Exception e) {
      /*
      some sort of error led to an Exception: handle this case via ValidationResult=ERROR
       */
      log.error(
          "{} while validating FHIR content: {}", e.getClass().getSimpleName(), e.getMessage());
      val svm = new SingleValidationMessage();
      svm.setMessage(e.getMessage());
      svm.setSeverity(ResultSeverityEnum.ERROR);
      return new ValidationResult(this.getContext(), List.of(svm));
    }
  }
}
