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

package de.gematik.bbriccs.vsdm.types;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.VsdmUtils;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public record VsdmKvnr(String kvnr) {

  /**
   * Requirement: A_27278 ; KVNR
   *
   * @return kvnr as byte array
   */
  public byte[] generate() {
    return kvnr.getBytes(StandardCharsets.US_ASCII);
  }

  public static VsdmKvnr parse(byte[] data, VsdmCheckDigitVersion version) {
    if (version == VsdmCheckDigitVersion.V1) {
      val kvnr = new String(VsdmUtils.copyByteArrayFrom(data, 0, 10), StandardCharsets.UTF_8);
      return new VsdmKvnr(kvnr);
    } else {
      val byteValue = VsdmUtils.copyByteArrayFrom(data, 8, data.length);
      val kvnr = new String(byteValue, StandardCharsets.UTF_8);
      return new VsdmKvnr(kvnr);
    }
  }

  @Override
  public String toString() {
    return kvnr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VsdmKvnr vsdmKvnr = (VsdmKvnr) o;
    return Objects.equals(kvnr, vsdmKvnr.kvnr);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(kvnr);
  }

  public static VsdmKvnr from(String kvnr) {
    return new VsdmKvnr(kvnr);
  }
}
