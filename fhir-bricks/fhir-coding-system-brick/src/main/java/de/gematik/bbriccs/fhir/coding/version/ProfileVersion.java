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

package de.gematik.bbriccs.fhir.coding.version;

public interface ProfileVersion {

  /**
   * Decide if a tailing SemVer PATCH should omit if the value is "0"
   *
   * @return true if zero PATCH in a version should be omitted and false otherwise
   */
  default boolean omitZeroPatch() {
    return true;
  }

  /**
   * Decide if a tailing SemVer PATCH should omit independent of the concrete value
   *
   * @return true if PATCH in a version should be omitted and false otherwise
   */
  default boolean omitPatch() {
    return false;
  }

  String getVersion();

  String getName();

  default boolean isEqual(String version) {
    return compareTo(version) == 0;
  }

  default int compareTo(ProfileVersion o) {
    if (!this.equals(o)) {
      return -1;
    }
    return compareTo(o.getVersion());
  }

  default int compareTo(String version) {
    return VersionUtil.compare(this.getVersion(), version);
  }
}
