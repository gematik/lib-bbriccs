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
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.validation.support.ErrorMessageFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;

@Slf4j
public class ProfiledValidator extends ValidatorFhirBase {

  @Getter private final String id;
  private final FhirContext ctx;
  private final FhirValidator validator;
  private final List<IValidationSupport> customProfileSupports;

  public ProfiledValidator(String id, List<IValidationSupport> customProfileSupports) {
    this(id, customProfileSupports, null);
  }

  public ProfiledValidator(
      String id,
      List<IValidationSupport> customProfileSupports,
      @Nullable ErrorMessageFilter errorFilter) {
    this(FhirContext.forR4(), id, customProfileSupports, errorFilter);
  }

  public ProfiledValidator(
      FhirContext ctx,
      String id,
      List<IValidationSupport> customProfileSupports,
      @Nullable ErrorMessageFilter errorFilter) {
    this.ctx = ctx;
    this.id = id;
    this.validator = ctx.newValidator();
    this.customProfileSupports = customProfileSupports;

    ctx.setParserErrorHandler(new StrictErrorHandler());

    // create support chain for validation
    // create support validators for custom profiles
    val validationSupports = new ArrayList<>(customProfileSupports);
    validationSupports.add(ctx.getValidationSupport());
    validationSupports.add(new InMemoryTerminologyServerValidationSupport(ctx));
    validationSupports.add(new SnapshotGeneratingValidationSupport(ctx));

    // configure the HAPI FhirParser
    val fiv = new FhirInstanceValidator(ctx);
    val validationSupportChain =
        new ValidationSupportChain(validationSupports.toArray(IValidationSupport[]::new));

    fiv.setValidationSupport(validationSupportChain);
    fiv.setErrorForUnknownProfiles(true);
    fiv.setNoExtensibleWarnings(true);
    fiv.setAnyExtensionsAllowed(false);

    this.validator.registerValidatorModule(fiv);
    if (errorFilter != null) this.validator.registerValidatorModule(errorFilter);
  }

  @Override
  public FhirContext getContext() {
    return this.ctx;
  }

  @Override
  public ValidationResult validate(String content) {
    val nullSafeContent = Objects.requireNonNullElse(content, "");
    return this.validateSafely(() -> this.validator.validateWithResult(nullSafeContent));
  }

  protected boolean doesSupport(String url) {
    var ret = false;

    for (val cps : this.customProfileSupports) {
      val sdef = cps.fetchStructureDefinition(url);
      if (sdef != null) {
        ret = true;
        break;
      }
    }

    return ret;
  }
}
