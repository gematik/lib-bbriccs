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

package de.gematik.bbriccs.profiles.utils;

import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import lombok.Getter;

@Getter
public class TestBasisClassVersion implements ProfileVersion {

  private final String version;
  public final Integer[] constructorCalls = {0, 0, 0};

  public TestBasisClassVersion() {
    this.version = "1.0.0";
    constructorCalls[0]++;
  }

  public TestBasisClassVersion(String version) {
    this.version = version;
    constructorCalls[1]++;
  }

  public TestBasisClassVersion(String version, boolean test) {
    this.version = version;
    constructorCalls[2]++;
  }
}