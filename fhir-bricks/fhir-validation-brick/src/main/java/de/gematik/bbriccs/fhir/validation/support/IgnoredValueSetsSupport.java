/*
 * Copyright (Change Date see Readme) gematik GmbH
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
import ca.uhn.fhir.context.support.IValidationSupport.CodeValidationResult;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.BaseValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;

@Slf4j
public class IgnoredValueSetsSupport extends BaseValidationSupport {

  private final String validatorId;
  private final HashSet<String> ignoredValueSets;

  public IgnoredValueSetsSupport(
      FhirContext theFhirContext, String validatorId, Collection<String> ignoredValueSets) {
    super(theFhirContext);
    this.validatorId = validatorId;
    this.ignoredValueSets = new HashSet<>(ignoredValueSets);
  }

  @Override
  public boolean isValueSetSupported(
      ValidationSupportContext theValidationSupportContext, String theValueSetUrl) {
    val resource =
        theValidationSupportContext
            .getRootValidationSupport()
            .fetchValueSet(Strings.nullToEmpty(theValueSetUrl));
    if (resource == null) return false;

    val valueSet = (ValueSet) resource;

    val isIgnored = ignoredValueSets.contains(valueSet.getUrl());

    if (isIgnored)
      log.trace("(validator {}) pretends to support ValueSet {}", validatorId, theValueSetUrl);

    return isIgnored;
  }

  @Override
  @Nullable
  public CodeValidationResult validateCodeInValueSet(
      ValidationSupportContext theValidationSupportContext,
      ConceptValidationOptions theOptions,
      String theCodeSystem,
      String theCode,
      String theDisplay,
      @Nonnull IBaseResource theValueSet) {
    val valueSet = (ValueSet) theValueSet;

    if (ignoredValueSets.contains(valueSet.getUrl())) {
      log.info(
          "(validator {}) Skip validation of value {} from set {}",
          validatorId,
          theCode,
          valueSet.getUrl());
      CodeValidationResult result = new CodeValidationResult();
      result.setCodeSystemName(theCodeSystem);
      result.setCode(theCode);
      result.setDisplay(theDisplay);
      return result;
    }

    return null;
  }
}
