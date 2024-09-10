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
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <a
 * href="https://simplifier.net/packages/de.basisprofil.r4/1.0.0/files/397841">de.basisprofil.r4</a>
 */
@Getter
@RequiredArgsConstructor
public enum InsuranceTypeDe implements ProfileValueSet {
  GKV("GKV", "gesetzliche Krankenversicherung"),
  PKV("PKV", "private Krankenversicherung"),
  BG("BG", "Berufsgenossenschaft"),
  SEL("SEL", "Selbstzahler"),
  SOZ("SOZ", "Sozialamt"),
  GPV("GPV", "gesetzliche Pflegeversicherung"),
  PPV("PPV", "private Pflegeversicherung"),
  BEI("BEI", "Beihilfe");

  public static final DeBasisProfilCodeSystem CODE_SYSTEM =
      DeBasisProfilCodeSystem.VERSICHERUNGSART_DE_BASIS;
  public static final String DESCRIPTION =
      "ValueSet zur Codierung der Versicherungsart in Deutschland";
  public static final String PUBLISHER = "HL7 Deutschland e.V. (Technisches Komitee FHIR)";

  private final String code;
  private final String display;

  @Override
  public DeBasisProfilCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static InsuranceTypeDe fromCode(String code) {
    return ProfileValueSet.fromCode(InsuranceTypeDe.class, code);
  }
}
