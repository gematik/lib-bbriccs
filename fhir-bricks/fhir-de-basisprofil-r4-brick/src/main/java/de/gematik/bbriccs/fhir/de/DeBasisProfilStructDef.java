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

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum DeBasisProfilStructDef implements WithStructureDefinition<DeBasisProfilVersion> {
  GKV_PERSON_GROUP("http://fhir.de/StructureDefinition/gkv/besondere-personengruppe"),
  GKV_DMP_KENNZEICHEN("http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen"),
  GKV_WOP("http://fhir.de/StructureDefinition/gkv/wop"),
  GKV_VERSICHERTENART("http://fhir.de/StructureDefinition/gkv/versichertenart"),
  NORMGROESSE("http://fhir.de/StructureDefinition/normgroesse"),
  HUMAN_NAMENSZUSATZ("http://fhir.de/StructureDefinition/humanname-namenszusatz"),
  IDENTIFIER_IKNR("http://fhir.de/StructureDefinition/identifier-iknr");

  private final String canonicalUrl;
}
