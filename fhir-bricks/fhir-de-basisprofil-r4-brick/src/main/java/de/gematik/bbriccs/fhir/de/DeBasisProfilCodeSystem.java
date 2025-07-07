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

package de.gematik.bbriccs.fhir.de;

import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum DeBasisProfilCodeSystem implements WithCodeSystem {
  LAENDERKENNZEICHEN("http://fhir.de/CodeSystem/deuev/anlage-8-laenderkennzeichen"),
  VERSICHERUNGSART_DE_BASIS("http://fhir.de/CodeSystem/versicherungsart-de-basis"),
  IDENTIFIER_TYPE_DE_BASIS("http://fhir.de/CodeSystem/identifier-type-de-basis"),
  NORMGROESSE("http://fhir.de/CodeSystem/normgroesse"),
  PZN("http://fhir.de/CodeSystem/ifa/pzn"),
  ATC("http://fhir.de/CodeSystem/bfarm/atc"),
  ASK("http://fhir.de/CodeSystem/ask"),
  ;

  private final String canonicalUrl;
}
