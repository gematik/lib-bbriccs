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

package de.gematik.bbriccs.fhir.vzd.r4;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.fhir.vzd.VzdCodeSystem;
import de.gematik.bbriccs.fhir.vzd.valueset.PharmacyHealthcareSpeciality;
import de.gematik.bbriccs.fhir.vzd.valueset.VzdOrigin;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.HealthcareService;

@Slf4j
@ResourceDef(name = "HealthcareService")
@SuppressWarnings({"java:S110"})
public class VzdHealthcareService extends HealthcareService {

  public VzdOrigin getOrigin() {
    return this.getMeta().getTag().stream()
        .filter(VzdCodeSystem.ORIGIN::matches)
        .map(VzdOrigin::from)
        .findFirst()
        .orElse(VzdOrigin.EMPTY);
  }

  public Optional<TelematikID> getTelematikID() {
    return this.getIdentifier().stream()
        .filter(TelematikID::matches)
        .map(TelematikID::from)
        .findFirst();
  }

  public List<PharmacyHealthcareSpeciality> getPharmacySpecialities() {
    return this.getSpecialty().stream()
        .flatMap(cc -> cc.getCoding().stream())
        .filter(PharmacyHealthcareSpeciality::matches)
        .map(PharmacyHealthcareSpeciality::from)
        .distinct()
        .toList();
  }
}
