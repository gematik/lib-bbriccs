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

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import org.hl7.fhir.r4.model.Coding;

@EqualsAndHashCode(callSuper = true)
public class ASK extends SemanticValue<String, DeBasisProfilCodeSystem> {

  private final String display;

  private ASK(String value, @Nullable String display) {
    super(DeBasisProfilCodeSystem.ASK, value);
    this.display = display;
  }

  public static ASK from(Coding coding) {
    return new ASK(coding.getCode(), coding.getDisplay());
  }

  public static ASK from(String code) {
    return new ASK(code, null);
  }

  public Optional<String> getDisplay() {
    return Optional.ofNullable(this.display);
  }
}
