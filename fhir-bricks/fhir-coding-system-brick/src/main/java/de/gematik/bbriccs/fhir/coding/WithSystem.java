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

import java.util.Arrays;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;

public interface WithSystem {

  String getCanonicalUrl();

  default boolean matches(Meta meta) {
    return this.matches(meta.getProfile().toArray(CanonicalType[]::new));
  }

  default boolean matches(Identifier... identifier) {
    return Arrays.stream(identifier).anyMatch(id -> this.matches(id.getSystem()));
  }

  default boolean matches(Coding... coding) {
    return Arrays.stream(coding).anyMatch(c -> this.matches(c.getSystem()));
  }

  default boolean matches(CodeableConcept... codeableConcepts) {
    return Arrays.stream(codeableConcepts)
        .flatMap(cc -> cc.getCoding().stream())
        .anyMatch(this::matches);
  }

  default boolean matches(CanonicalType... canonicalType) {
    return Arrays.stream(canonicalType).anyMatch(ct -> this.matches(ct.asStringValue()));
  }

  default boolean matches(WithSystem... other) {
    return Arrays.stream(other).anyMatch(ws -> this.matches(ws.getCanonicalUrl()));
  }

  default boolean matches(String url) {
    if (url == null) {
      return false;
    }
    val withoutVersion = url.split("\\|")[0];
    return this.getCanonicalUrl().equals(withoutVersion);
  }
}
