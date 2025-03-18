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

package de.gematik.bbriccs.fhir.de.value;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import org.hl7.fhir.r4.model.Coding;

/**
 * ATC is an Coding System by BFarm to classify 'Anatomisch-Therapeutisch-Chemischen (ATC)
 * Klassifikation' details under: "<a
 * href="https://www.bfarm.de/DE/Kodiersysteme/Klassifikationen/ATC/_node.html">...</a>"
 */
@EqualsAndHashCode(callSuper = true)
public class ATC extends SemanticValue<String, DeBasisProfilCodeSystem> {

  private final String display;
  private final String version;

  private ATC(String value, @Nullable String display, @Nullable String version) {
    super(DeBasisProfilCodeSystem.ATC, value);
    this.display = display;
    this.version = version;
  }

  public static ATC from(Coding coding) {
    return new ATC(coding.getCode(), coding.getDisplay(), coding.getVersion());
  }

  public static ATC from(String code, String display, @Nullable String version) {
    return new ATC(code, display, version);
  }

  public static ATC from(String code, @Nullable String display) {
    return from(code, display, null);
  }

  public static ATC from(String code) {
    return from(code, null);
  }

  public Optional<String> getDisplay() {
    return Optional.ofNullable(this.display);
  }

  public Optional<String> getVersion() {
    return Optional.ofNullable(this.version);
  }
}
