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

package de.gematik.bbriccs.rest.fd;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.fd.FhirClientImpl.FdClientBuilder;
import org.hl7.fhir.r4.model.Resource;

public interface FhirClient {

  <R extends Resource> String encode(R resource);

  <R extends Resource> String encode(R resource, boolean prettyPrint);

  <R extends Resource> R decode(Class<R> type, String content);

  boolean isValid(String content);

  ValidationResult validate(String content);

  <T extends Resource, R extends Resource> FhirBResponse<R> request(FhirBRequest<T, R> request);

  static FdClientBuilder via(HttpBClient httpClient) {
    return FhirClientImpl.via(httpClient);
  }
}
