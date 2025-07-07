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
import ca.uhn.fhir.validation.ValidationResult;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

public interface ValidatorFhir {

  /**
   * Get the {@link FhirContext} used by this validator
   *
   * @return the {@link FhirContext} used for validation
   */
  FhirContext getContext();

  /**
   * Perform a validation on the given string content
   *
   * @param content to be validated
   * @return the {@link ValidationResult}
   */
  ValidationResult validate(String content);

  /**
   * Perform a validation on the given {@link IBaseResource}.
   *
   * <p>As default implementation, the resource is serialized to a json-string and then validated.
   * This is basically the same as behaviour the HAPI does under the hood with the only difference
   * that the HAPI implementation does override resource-ids with bundle-entry-full-urls by default
   * which can lead to issues.
   *
   * <p><b>NOTE:</b> use with caution as the HAPI validation on {@link IBaseResource} (especially in
   * {@link Bundle} Resources) is quite buggy. Therefore, prefer validating on the raw content
   * whenever possible.
   *
   * @param resource to be validated
   * @return the {@link ValidationResult}
   */
  default ValidationResult validate(IBaseResource resource) {
    val parser =
        this.getContext()
            .newXmlParser()
            .setOverrideResourceIdWithBundleEntryFullUrl(false)
            .setOmitResourceId(false);
    val content = parser.encodeToString(resource);
    return this.validate(content);
  }

  /**
   * Perform a validation on the given content and extract the overall result from the {@link
   * ValidationResult}
   *
   * @param content to be validated
   * @return true if the {@link ValidationResult} was successful or false otherwise
   */
  default boolean isValid(String content) {
    return this.validate(content).isSuccessful();
  }

  /**
   * Perform a validation on the given resource and extract the overall result from the {@link
   * ValidationResult}
   *
   * @param resource to be validated
   * @return true if the {@link ValidationResult} was successful or false otherwise
   */
  default boolean isValid(IBaseResource resource) {
    return this.validate(resource).isSuccessful();
  }
}
