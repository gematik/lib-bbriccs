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

package de.gematik.bbriccs.fhir.de.valueset;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <br>
 * <b>Profile:</b> de.basisprofil.r4 (0.9.13) <br>
 * <b>File:</b> ValueSet-identifier-type-de-basis.json <br>
 * <br>
 * <b>Publisher:</b> HL7 Deutschland e.V. (Technisches Komitee FHIR) <br>
 * <b>Published:</b> 2020-01-10 <br>
 * <b>Status:</b> draft
 */
@Getter
@RequiredArgsConstructor
public enum IdentifierTypeDe implements FromValueSet {
  GKV("GKV", "Gesetzliche Krankenversicherung"),
  PKV("PKV", "Private Krankenversicherung"),
  LANR("LANR", "Lebenslange Arztnummer"),
  ZANR("ZANR", "Zahnarztnummer"),
  BSNR("BSNR", "Betriebsstättennummer"),
  KZVA("KZVA", "KZVAbrechnungsnummer"),
  KVZ10("KVZ10", "Krankenversichertennummer"),
  ;

  public static final DeBasisProfilCodeSystem CODE_SYSTEM =
      DeBasisProfilCodeSystem.IDENTIFIER_TYPE_DE_BASIS;

  private final String code;
  private final String display;

  @Override
  public DeBasisProfilCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static IdentifierTypeDe fromCode(String code) {
    return FromValueSet.fromCode(IdentifierTypeDe.class, code);
  }
}
