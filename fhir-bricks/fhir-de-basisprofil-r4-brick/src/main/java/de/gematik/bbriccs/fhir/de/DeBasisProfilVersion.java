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

import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.coding.version.VersionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeBasisProfilVersion implements ProfileVersion {
  V0_9_13("0.9.13"),
  V1_3_2("1.3.2"),
  V1_4_0("1.4.0");

  private final String version;

  public static DeBasisProfilVersion fromString(String input) {
    return VersionUtil.fromString(DeBasisProfilVersion.class, input);
  }

  public static DeBasisProfilVersion getDefaultVersion() {
    return VersionUtil.getDefaultVersion(DeBasisProfilVersion.class, "de.basisprofil.r4");
  }
}
