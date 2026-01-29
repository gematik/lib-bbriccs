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

package de.gematik.bbriccs.fhir.validation.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.BaseValidationSupport;

@Slf4j
public class IgnoredCodeSystemsSupport extends BaseValidationSupport {

  private final String validatorId;
  private final HashSet<String> ignoredCodeSystems;

  /**
   * Constructor
   *
   * @param theFhirContext {@link FhirContext}
   */
  public IgnoredCodeSystemsSupport(
      FhirContext theFhirContext, String validatorId, Collection<String> ignoredCodeSystems) {
    super(theFhirContext);
    this.validatorId = validatorId;
    this.ignoredCodeSystems = new HashSet<>(ignoredCodeSystems);
  }

  @Override
  public boolean isCodeSystemSupported(
      ValidationSupportContext theValidationSupportContext, String theCodeSystem) {
    return theCodeSystem != null && this.ignoredCodeSystems.contains(theCodeSystem);
  }

  @Nullable
  @Override
  public CodeValidationResult validateCode(
      @Nonnull ValidationSupportContext theValidationSupportContext,
      @Nonnull ConceptValidationOptions theOptions,
      String theCodeSystem,
      String theCode,
      String theDisplay,
      String theValueSetUrl) {

    if (isCodeSystemSupported(theValidationSupportContext, theCodeSystem)) {
      log.info(
          "(validator {}) Skip validation of code {} from system {}",
          validatorId,
          theCode,
          theCodeSystem);
      val result = new CodeValidationResult();
      result.setCodeSystemName(theCodeSystem);
      result.setCode(theCode);
      result.setDisplay(theDisplay);
      return result;
    } else {
      return null;
    }
  }
}
