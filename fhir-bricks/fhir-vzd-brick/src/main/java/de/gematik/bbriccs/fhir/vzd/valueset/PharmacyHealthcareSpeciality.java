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

package de.gematik.bbriccs.fhir.vzd.valueset;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.vzd.VzdCodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Coding;

@Getter
@RequiredArgsConstructor
public enum PharmacyHealthcareSpeciality implements FromValueSet {
  HAND_SALE("10", "Handverkauf"),
  NIGHT_EMERGENCY_SERVICE("20", "Nacht- und Notdienst"),
  COURIER_SERVICE("30", "Botendienst"),
  DELIVERY("40", "Versand"),
  STERILE_MANUFACTURING("50", "Sterilherstellung");

  private static final VzdCodeSystem CODE_SYSTEM = VzdCodeSystem.PHARMACY_SPECIALITY;

  private final String code;
  private final String display;

  @Override
  public VzdCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static PharmacyHealthcareSpeciality from(Coding coding) {
    return FromValueSet.fromCode(PharmacyHealthcareSpeciality.class, coding.getCode());
  }

  public static boolean matches(Coding coding) {
    return CODE_SYSTEM.matches(coding);
  }
}
