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

package de.gematik.bbriccs.vsdm;

import de.gematik.bbriccs.vsdm.exceptions.ParsingException;
import java.util.Arrays;

public class VsdmUtils {

  private VsdmUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static byte[] copyByteArrayFrom(byte[] data, int from, int to) throws ParsingException {
    if (data == null || data.length == 0) {
      throw new ParsingException();
    }
    if (to > data.length) {
      throw new ParsingException(data, from, to);
    }
    try {
      return Arrays.copyOfRange(data, from, to);
    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
      throw new ParsingException(data, from, to);
    }
  }

  public static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      hexString.append(String.format("%02X ", b));
    }
    return hexString.toString();
  }

  public static byte[] long2ByteArray(long value, int nrOfBytes) {
    byte[] result = new byte[nrOfBytes];
    for (int i = 0; i < nrOfBytes && i < 8; i++) {
      result[nrOfBytes - 1 - i] = (byte) (value >>> (i * 8));
    }
    return result;
  }
}
