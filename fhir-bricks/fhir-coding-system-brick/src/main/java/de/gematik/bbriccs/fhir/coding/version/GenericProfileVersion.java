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

package de.gematik.bbriccs.fhir.coding.version;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class GenericProfileVersion implements ProfileVersion {

  private final String version;
  private final boolean omitZeroPatch;

  public GenericProfileVersion(String version) {
    this(version, true);
  }

  public GenericProfileVersion(String version, boolean omitZeroPatch) {
    this.version = version;
    this.omitZeroPatch = omitZeroPatch;
  }

  @Override
  public boolean omitZeroPatch() {
    return this.omitZeroPatch;
  }
}
