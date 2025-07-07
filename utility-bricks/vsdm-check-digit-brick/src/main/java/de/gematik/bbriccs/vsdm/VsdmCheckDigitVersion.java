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

package de.gematik.bbriccs.vsdm;

import java.util.Base64;

public enum VsdmCheckDigitVersion {
  V1,
  V2;

  public static VsdmCheckDigitVersion fromData(byte[] data) {
    // A_27278
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Data is empty");
    }
    int firstByte = data[0] & 0xFF;
    return firstByte > 128 ? V2 : V1;
  }

  public static VsdmCheckDigitVersion fromData(String data) {
    return fromData(Base64.getDecoder().decode(data));
  }
}
