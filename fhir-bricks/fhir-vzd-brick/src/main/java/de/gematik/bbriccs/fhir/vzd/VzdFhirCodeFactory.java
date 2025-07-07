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

package de.gematik.bbriccs.fhir.vzd;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import de.gematik.bbriccs.fhir.vzd.r4.VzdHealthcareService;
import de.gematik.bbriccs.fhir.vzd.r4.VzdLocation;
import de.gematik.bbriccs.fhir.vzd.r4.VzdOrganization;

public interface VzdFhirCodeFactory {

  static FhirCodec withoutValidation() {
    return initializeWithTypeHints().andDummyValidator();
  }

  static FhirCodec withValidation(ValidatorFhir validator) {
    return initializeWithTypeHints().andCustomValidator(validator);
  }

  static FhirCodec.FhirCodecBuilder initializeWithTypeHints() {
    return FhirCodec.forR4()
        .withTypeHint(VzdStructDef.HEALTH_CARE_SERVICE, VzdHealthcareService.class)
        .withTypeHint(VzdStructDef.HEALTH_CARE_SERVICE_STRICT, VzdHealthcareService.class)
        .withTypeHint(VzdStructDef.ORGANIZATION, VzdOrganization.class)
        .withTypeHint(VzdStructDef.ORGANIZATION_STRICT, VzdOrganization.class)
        .withTypeHint(VzdStructDef.LOCATION, VzdLocation.class)
        .withTypeHint(VzdStructDef.LOCATION_STRICT, VzdLocation.class);
  }
}
