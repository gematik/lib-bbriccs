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

package de.gematik.bbriccs.fhir.de;

import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum HL7CodeSystem implements WithCodeSystem {
  HL7_V2_0203("http://terminology.hl7.org/CodeSystem/v2-0203"),
  CONSENT_SCOPE("http://terminology.hl7.org/CodeSystem/consentscope"),
  ACT_CODE("http://terminology.hl7.org/CodeSystem/v3-ActCode"),
  ASK_CODE("http://fhir.de/CodeSystem/ask"),
  DATA_ABSENT("http://terminology.hl7.org/CodeSystem/data-absent-reason");

  private final String canonicalUrl;
}
