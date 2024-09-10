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

package de.gematik.bbriccs.fhir.de.valueset;

import de.gematik.bbriccs.fhir.coding.ProfileValueSet;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * https://terminology.hl7.org/2.1.0/CodeSystem-v3-ActCode.html Note: not complete, extend on demand
 */
@Getter
@RequiredArgsConstructor
public enum ActCode implements ProfileValueSet {
  OPTIN(
      "OPTIN",
      "opt-in",
      "A grantor's assent to the terms of an agreement offered by a grantee without an opportunity"
          + " for to dissent to any terms."),
  OPTINR(
      "OPTINR",
      "opt-in with restrictions",
      "A grantor's assent to the grantee's terms of an agreement with an opportunity for to dissent"
          + " to certain grantor or grantee selected terms."),
  OPTOUT(
      "OPTOUT",
      "opt-out",
      "A grantor's dissent to the terms of agreement offered by a grantee without an opportunity"
          + " for to assent to any terms."),
  OPTOUTE(
      "OPTOUTE",
      "opt-out with exceptions",
      "A grantor's dissent to the grantee's terms of agreement except for certain grantor or"
          + " grantee selected terms.");

  public static final HL7CodeSystem CODE_SYSTEM = HL7CodeSystem.ACT_CODE;
  public static final String DESCRIPTION =
      "A code specifying the particular kind of Act that the Act-instance represents within its"
          + " class.";
  public static final String PUBLISHER = "Health Level 7";

  private final String code;
  private final String display;
  private final String definition;

  @Override
  public HL7CodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ActCode fromCode(String code) {
    return ProfileValueSet.fromCode(ActCode.class, code);
  }
}
