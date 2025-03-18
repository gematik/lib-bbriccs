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
 */

package de.gematik.bbriccs.vsdm;

import de.gematik.bbriccs.vsdm.types.VsdmKvnr;
import de.gematik.bbriccs.vsdm.types.VsdmPatient;
import de.gematik.bbriccs.vsdm.types.VsdmVendorIdentifier;
import java.time.LocalDate;
import lombok.val;

public class VsdmCheckDigitFactory {

  private VsdmCheckDigitFactory() {
    throw new IllegalStateException("Factory class");
  }

  public static VsdmCheckDigitV1 createV1(String kvnr, char identifier) {
    val patient = new VsdmPatient(VsdmKvnr.from(kvnr));
    return new VsdmCheckDigit(
        patient, VsdmVendorIdentifier.from(identifier, VsdmCheckDigitVersion.V1));
  }

  public static VsdmCheckDigitV2 createV2(String kvnr, char identifier) {
    val patient =
        new VsdmPatient(
            VsdmKvnr.from(kvnr), false, LocalDate.now().minusDays(365), "ExampleStreet");
    return createV2(patient, identifier);
  }

  public static VsdmCheckDigitV2 createV2(VsdmPatient patient, char identifier) {
    return new VsdmCheckDigit(
        patient, VsdmVendorIdentifier.from(identifier, VsdmCheckDigitVersion.V2));
  }
}
