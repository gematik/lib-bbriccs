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

import ca.uhn.fhir.validation.ValidationResult;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;

@Slf4j
public abstract class ValidatorFhirBase implements ValidatorFhir {

  protected final ProfileExtractor profileExtractor = new ProfileExtractor();

  protected ValidationResult validateSafely(Supplier<ValidationResult> validationResultSupplier) {
    try {
      return validationResultSupplier.get();
    } catch (Exception e) {
      /*
      some sort of error led to an Exception: handle this case via ValidationResult=ERROR
       */
      log.error(
          "{} while validating FHIR content: {}", e.getClass().getSimpleName(), e.getMessage());
      val svm = ValidationMessageUtil.createErrorMessage(e.getMessage());
      return new ValidationResult(this.getContext(), List.of(svm));
    }
  }

  protected boolean hasProfile(IBaseResource resource) {
    return !resource.getMeta().getProfile().stream()
        .map(IPrimitiveType::getValue)
        .filter(p -> !Strings.isNullOrEmpty(p))
        .toList()
        .isEmpty();
  }

  protected boolean isCollectionBundle(IBaseResource resource) {
    return Optional.of(resource)
        .filter(Bundle.class::isInstance)
        .map(r -> (Bundle) r)
        .filter(
            bundle ->
                bundle.getType() == BundleType.COLLECTION
                    || bundle.getType() == BundleType.SEARCHSET)
        .isPresent();
  }
}
