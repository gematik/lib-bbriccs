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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.validation.support.ErrorMessageFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;

/** A generic HAPI Validator which does not know any profiles */
@Slf4j
public class NonProfiledValidator extends ValidatorFhirBase {

  private final FhirContext ctx;
  private final FhirValidator validator;

  public NonProfiledValidator() {
    this(FhirContext.forR4());
  }

  public NonProfiledValidator(FhirContext ctx) {
    this.ctx = ctx;
    this.validator = ctx.newValidator();

    val validationSupports = new ArrayList<IValidationSupport>();
    validationSupports.add(ctx.getValidationSupport());
    validationSupports.add(new InMemoryTerminologyServerValidationSupport(ctx));
    validationSupports.add(new SnapshotGeneratingValidationSupport(ctx));

    // configure the HAPI FhirParser
    val fiv = new FhirInstanceValidator(ctx);
    val validationSupportChain =
        new ValidationSupportChain(validationSupports.toArray(IValidationSupport[]::new));

    fiv.setValidationSupport(validationSupportChain);
    fiv.setErrorForUnknownProfiles(false);
    fiv.setNoTerminologyChecks(true);
    fiv.setNoExtensibleWarnings(true);
    fiv.setAnyExtensionsAllowed(true);

    this.validator.registerValidatorModule(fiv);

    // the generic validator does not know any profiles!
    val filter = new LinkedList<String>();
    filter.add("^Profile reference '.*' has not been checked because it is unknown$");
    filter.add("^Unknown extension .*");

    this.validator.registerValidatorModule(new ErrorMessageFilter(filter));
  }

  @Override
  public FhirContext getContext() {
    return this.ctx;
  }

  @Override
  public ValidationResult validate(String content) {
    return this.validateSafely(() -> this.validator.validateWithResult(content));
  }
}
