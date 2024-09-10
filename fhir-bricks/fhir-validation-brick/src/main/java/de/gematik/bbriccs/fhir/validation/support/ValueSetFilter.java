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

package de.gematik.bbriccs.fhir.validation.support;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.BaseValidationSupport;

public class ValueSetFilter extends BaseValidationSupport {

  private final HashSet<String> omittedCodeSystems;

  /**
   * Constructor
   *
   * @param theFhirContext {@link FhirContext}
   */
  public ValueSetFilter(FhirContext theFhirContext, Collection<String> omittedCodeSystems) {
    super(theFhirContext);
    this.omittedCodeSystems = new HashSet<>(omittedCodeSystems);
  }

  @Override
  public boolean isCodeSystemSupported(
      ValidationSupportContext theValidationSupportContext, String theCodeSystem) {
    return theCodeSystem != null && this.omittedCodeSystems.contains(theCodeSystem);
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
      val result = new CodeValidationResult();
      result.setSeverity(IssueSeverity.INFORMATION);
      result.setCodeSystemName(theCodeSystem);
      result.setCode(theCode);
      result.setMessage(format("This module has no support for code system {0}", theCodeSystem));
      return result;
    } else {
      return null;
    }
  }
}
