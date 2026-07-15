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

package de.gematik.bbriccs.fhir.codec;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.FhirCodecImpl.FhirCodecBuilder;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;

public interface FhirCodec extends ValidatorFhir {

  String encode(IBaseResource resource, EncodingType encoding);

  String encode(IBaseResource resource, EncodingType encoding, boolean prettyPrint);

  <T extends Resource> T decode(Class<T> expectedClass, String content);

  <T extends Resource> T decode(Class<T> expectedClass, String content, EncodingType encoding);

  Resource decode(String content);

  Resource decode(String content, EncodingType encoding);

  FhirContext getContext();

  /**
   * Creates a codec builder preconfigured for FHIR R4.
   *
   * @return a builder backed by an R4 {@link FhirContext}
   */
  static FhirCodecBuilder forR4() {
    return new FhirCodecBuilder(FhirContext.forR4());
  }
}
