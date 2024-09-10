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
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.refv.commons.validation.ValidationModule;
import lombok.SneakyThrows;
import lombok.val;

public class ReferenzValidator implements ValidatorFhir {

  private final ValidationModule validationModule;
  private final FhirContext context; // required to be able to map back refVR to HAPIs VR

  public ReferenzValidator(FhirContext ctx, ValidationModule validationModule) {
    this.context = ctx;
    this.validationModule = validationModule;
  }

  @Override
  public FhirContext getContext() {
    return this.context;
  }

  @Override
  public ValidationResult validate(String content) {
    val refVr = this.validationModule.validateString(content);
    return new ValidationResult(this.getContext(), refVr.getValidationMessages().stream().toList());
  }

  public static ValidatorFhir withValidationModule(SupportedValidationModule svm) {
    return withValidationModule(FhirContext.forR4(), svm);
  }

  @SneakyThrows
  public static ValidatorFhir withValidationModule(FhirContext ctx, SupportedValidationModule svm) {
    return withValidationModule(ctx, new ValidationModuleFactory().createValidationModule(svm));
  }

  public static ValidatorFhir withValidationModule(
      FhirContext ctx, ValidationModule validationModule) {
    return new ReferenzValidator(ctx, validationModule);
  }
}
