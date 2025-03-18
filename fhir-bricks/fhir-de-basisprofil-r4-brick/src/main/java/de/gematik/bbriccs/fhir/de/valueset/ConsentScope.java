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

package de.gematik.bbriccs.fhir.de.valueset;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import lombok.Getter;

/**
 * <a
 * href="https://terminology.hl7.org/2.1.0/CodeSystem-consentscope.html">CodeSystem-consentscope</a>
 */
@Getter
public enum ConsentScope implements FromValueSet {
  RESEARCH(
      "research",
      "Research",
      "Consent to participate in research protocol and information sharing required"),
  PATIENT_PRIVACY(
      "patient-privacy",
      "Privacy Consent",
      "Agreement to collect, access, use or disclose (share) information"),
  TREATMENT("treatment", "Treatment", "Consent to undergo a specific treatment");

  public static final HL7CodeSystem CODE_SYSTEM = HL7CodeSystem.CONSENT_SCOPE;

  private final String code;
  private final String display;
  private final String definition;

  ConsentScope(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public HL7CodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ConsentScope fromCode(String code) {
    return FromValueSet.fromCode(ConsentScope.class, code);
  }
}
