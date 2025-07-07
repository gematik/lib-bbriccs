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

package de.gematik.bbriccs.fhir.de.value;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidSystemException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;

public class TelematikID extends SemanticValue<String, DeBasisProfilNamingSystem> {

  private TelematikID(String telematikId) {
    super(DeBasisProfilNamingSystem.TELEMATIK_ID_SID, telematikId);
  }

  public static TelematikID random() {
    val faker = FakerBrick.getGerman();
    // 3-SMC-B-Testkarte-883110000116873
    val id = format("3-SMC-B-Testkarte-{0}", faker.regexify("[0-9]{15}"));
    return from(id);
  }

  public static TelematikID from(String value) {
    return new TelematikID(value);
  }

  public static TelematikID from(Identifier identifier) {
    return Optional.of(identifier)
        .filter(DeBasisProfilNamingSystem.TELEMATIK_ID_SID::matches)
        .map(id -> TelematikID.from(id.getValue()))
        .orElseThrow(
            () ->
                new InvalidSystemException(
                    format(
                        "Cannot extract TelematikID from identifier with system {0}",
                        identifier.getSystem())));
  }

  public static boolean matches(Identifier identifier) {
    return DeBasisProfilNamingSystem.TELEMATIK_ID_SID.matches(identifier);
  }
}
