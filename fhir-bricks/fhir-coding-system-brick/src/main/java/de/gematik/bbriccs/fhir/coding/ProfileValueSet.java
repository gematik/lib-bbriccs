/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.bbriccs.fhir.coding;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

/** Representation for predefined ValueSets (Schlüsseltabellen) */
public interface ProfileValueSet {

  String getCode();

  String getDisplay();

  WithCodeSystem getCodeSystem();

  default CodeableConcept asCodeableConcept() {
    return asCodeableConcept(false);
  }

  /**
   * This method is required because depending on the version of the used profile, the concrete
   * system can vary.
   *
   * @param codeSystem to be used to denote the identifier
   * @return this value as a CodeableConcept
   */
  default CodeableConcept asCodeableConcept(WithCodeSystem codeSystem) {
    return asCodeableConcept(codeSystem, false);
  }

  default CodeableConcept asCodeableConcept(boolean withDisplay) {
    val coding = asCoding(withDisplay);
    return new CodeableConcept().setCoding(List.of(coding));
  }

  /**
   * This method is required because depending on the version of the used profile, the concrete
   * system can vary.
   *
   * @param codeSystem to be used to denote the identifier
   * @param withDisplay allows deciding if the display value should be coded into the
   *     CodeableConcept
   * @return this value as a CodeableConcept
   */
  default CodeableConcept asCodeableConcept(WithCodeSystem codeSystem, boolean withDisplay) {
    val coding = asCoding(codeSystem, withDisplay);
    return new CodeableConcept().setCoding(List.of(coding));
  }

  default Coding asCoding() {
    return asCoding(false);
  }

  default Coding asCoding(WithCodeSystem codeSystem) {
    return asCoding(codeSystem, false);
  }

  default Coding asCoding(boolean withDisplay) {
    return asCoding(this.getCodeSystem(), withDisplay);
  }

  default Coding asCoding(WithCodeSystem codeSystem, boolean withDisplay) {
    val coding = new Coding();
    coding.setCode(this.getCode());
    coding.setSystem(codeSystem.getCanonicalUrl());
    if (withDisplay) {
      coding.setDisplay(getDisplay());
    }
    return coding;
  }

  static <V extends Enum<?> & ProfileValueSet> V fromCode(Class<V> clazz, String code) {
    return Arrays.stream(clazz.getEnumConstants())
        .filter(pt -> pt.getCode().equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(clazz, code));
  }
}
