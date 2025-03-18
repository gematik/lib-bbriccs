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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.exceptions.ParsingException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public record VsdmVendorIdentifier(char identifier, VsdmCheckDigitVersion version) {
  private static final int KEY_VERSION_INDEX_V1 = 21;
  private static final int KEY_VERSION_INDEX_V2 = 0;

  static void checkIdentifier(char identifier) {
    if (identifier < 'A' || identifier > 'Z') {
      log.warn(format("Identifier ''{0}'' is not a capital letter", identifier));
    }
  }

  public byte generate() {
    if (version == VsdmCheckDigitVersion.V1) {
      return (byte) identifier();
    } else {
      return (byte) ((identifier() - 65) << 2);
    }
  }

  public static VsdmVendorIdentifier parseV1(byte[] data) {
    try {
      val identifier = (char) data[KEY_VERSION_INDEX_V1];
      checkIdentifier(identifier);
      return new VsdmVendorIdentifier(identifier, VsdmCheckDigitVersion.V1);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ParsingException(data, KEY_VERSION_INDEX_V1);
    }
  }

  public static VsdmVendorIdentifier parseV2(byte[] data, VsdmKeyVersion keyVersion) {
    try {
      int bkD4 = (data[KEY_VERSION_INDEX_V2] & 0xFF) - 128 - (keyVersion.keyVersion() - '0');
      val identifier = (char) ((bkD4 >> 2) + 'A');
      checkIdentifier(identifier);
      return new VsdmVendorIdentifier(identifier, VsdmCheckDigitVersion.V2);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ParsingException(data, KEY_VERSION_INDEX_V2);
    }
  }

  @Override
  public String toString() {
    return "" + identifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (o.getClass() == VsdmVendorIdentifier.class) {
      val otherIdentifier = (VsdmVendorIdentifier) o;
      return Objects.equals(identifier, otherIdentifier.identifier);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(identifier);
  }

  public static VsdmVendorIdentifier from(char identifier, VsdmCheckDigitVersion version) {
    return new VsdmVendorIdentifier(identifier, version);
  }
}
