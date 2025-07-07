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

package de.gematik.bbriccs.fhir.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.refv.commons.validation.ValidationModule;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;

@Slf4j
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

    try {
      val refVr = this.validationModule.validateString(content);
      return new ValidationResult(
          this.getContext(), refVr.getValidationMessages().stream().toList());
    } catch (Exception e) {
      /*
      some sort of error led to an Exception: handle this case via ValidationResult=ERROR
       */
      log.error("Error while validating FHIR content", e);
      val svm = new SingleValidationMessage();
      svm.setMessage(e.getMessage());
      svm.setSeverity(ResultSeverityEnum.ERROR);
      return new ValidationResult(this.getContext(), List.of(svm));
    }
  }

  @Override
  public ValidationResult validate(IBaseResource resource) {
    // reference-validator does not support IBaseResource validation directly
    val content =
        this.context
            .newXmlParser()
            .setOverrideResourceIdWithBundleEntryFullUrl(false)
            .encodeResourceToString(resource);
    return validate(content);
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
