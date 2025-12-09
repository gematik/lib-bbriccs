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

  default boolean isSmallerThan(ProfileVersion o) {
    return this.compareTo(o) < 0;
  }

  default boolean isSmallerThanOrEqualTo(ProfileVersion o) {
    return this.compareTo(o) <= 0;
  }

  default boolean isBiggerThan(ProfileVersion o) {
    return this.compareTo(o) > 0;
  }

  default boolean isBiggerThanOrEqualTo(ProfileVersion o) {
    return this.compareTo(o) >= 0;
  }

  /**
   * Compare two ProfileVersion objects by their concrete version value
   *
   * @param o is the specified object which is compared to this
   * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
   *     or greater than the specified object.
   */
  default int compareTo(ProfileVersion o) {
    return compareTo(o.getVersion());
  }

  default int compareTo(String version) {
    return VersionUtil.compare(this.getVersion(), version);
  }

  default boolean isEqual(ProfileVersion o) {
    return isEqual(o.getVersion());
  }

  default boolean isEqual(String version) {
    return compareTo(version) == 0;
  }
}
