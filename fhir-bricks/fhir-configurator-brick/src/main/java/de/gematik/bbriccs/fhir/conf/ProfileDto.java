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

package de.gematik.bbriccs.fhir.conf;

import static java.text.MessageFormat.format;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.val;

@Data
public class ProfileDto {
  private String name;
  private String version;
  private List<String> compatibleVersions = List.of();
  private List<String> canonicalClaims = List.of();
  private List<String> omitProfiles = List.of();

  @Override
  public String toString() {
    return format("{0}-{1}", name, version);
  }

  public List<String> getAllVersions() {
    val allVersions = new LinkedList<>(this.compatibleVersions);
    allVersions.addFirst(this.getVersion());
    return allVersions;
  }
}
