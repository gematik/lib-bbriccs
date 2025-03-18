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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import java.util.List;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class DummyValidator implements ValidatorFhir {

  private final FhirContext ctx;

  public DummyValidator(FhirContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public FhirContext getContext() {
    return this.ctx;
  }

  @Override
  public ValidationResult validate(String content) {
    return this.getValidationResult();
  }

  @Override
  public ValidationResult validate(IBaseResource resource) {
    return this.getValidationResult();
  }

  @Override
  public boolean isValid(String content) {
    return true;
  }

  private ValidationResult getValidationResult() {
    val svm = new SingleValidationMessage();
    svm.setSeverity(ResultSeverityEnum.INFORMATION);
    svm.setMessage(format("Information provided by {0}", this.getClass().getSimpleName()));
    return new ValidationResult(this.ctx, List.of(svm));
  }
}
