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

package de.gematik.bbriccs.fhir.de;

import de.gematik.bbriccs.fhir.coding.WithNamingSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum DeBasisProfilNamingSystem implements WithNamingSystem {
  IKNR("http://fhir.de/NamingSystem/arge-ik/iknr"),
  IKNR_SID("http://fhir.de/sid/arge-ik/iknr"),
  KVID("http://fhir.de/NamingSystem/gkv/kvid-10"),
  KVID_GKV_SID("http://fhir.de/sid/gkv/kvid-10"),
  KVID_PKV_SID("http://fhir.de/sid/pkv/kvid-10"),
  TELEMATIK_ID_SID("https://gematik.de/fhir/sid/telematik-id"),
  STANDORTNUMMER("http://fhir.de/sid/dkgev/standortnummer"),
  KZBV_ZAHNARZTNUMMER("http://fhir.de/NamingSystem/kzbv/zahnarztnummer"),
  KZBV_KZVA_ABRECHNUNGSNUMMER("http://fhir.de/NamingSystem/kzbv/kzvabrechnungsnummer"),
  KZBV_KZVA_ABRECHNUNGSNUMMER_SID("http://fhir.de/sid/kzbv/kzvabrechnungsnummer"),
  ASV_TEAMNUMMER("http://fhir.de/NamingSystem/asv/teamnummer");

  private final String canonicalUrl;
}
