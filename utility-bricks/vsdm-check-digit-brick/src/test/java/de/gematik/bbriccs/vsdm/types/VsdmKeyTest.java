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

package de.gematik.bbriccs.vsdm.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.VsdmUtils;
import de.gematik.bbriccs.vsdm.exceptions.InvalidKeyLengthException;
import javax.crypto.spec.SecretKeySpec;
import lombok.val;
import org.bouncycastle.crypto.params.KeyParameter;
import org.junit.jupiter.api.Test;

class VsdmKeyTest {
  @Test
  void shouldThrowExceptionForInvalidKeyLength() {
    byte[] invalidKey = new byte[16];
    val keyVersion = new VsdmKeyVersion('1', VsdmCheckDigitVersion.V1);
    assertThrows(InvalidKeyLengthException.class, () -> new VsdmKey(invalidKey, keyVersion));
  }

  @Test
  void shouldReturnKeyParameterForVersion1() {
    byte[] validKey = new byte[32];
    VsdmKey vsdmKey = new VsdmKey(validKey, new VsdmKeyVersion('1', VsdmCheckDigitVersion.V1));
    KeyParameter keyParameter = vsdmKey.getKeyForVersion1();
    assertArrayEquals(validKey, keyParameter.getKey());
  }

  @Test
  void shouldDeriveAesKeyForVersion2() {
    byte[] validKey = new byte[32];
    validKey[31] = (byte) 0x01;

    VsdmKey vsdmKey = new VsdmKey(validKey, new VsdmKeyVersion('2', VsdmCheckDigitVersion.V2));
    SecretKeySpec aesKey = vsdmKey.getKeyForVersion2();
    assertEquals(16, aesKey.getEncoded().length);
    assertEquals("AES", aesKey.getAlgorithm());
    assertEquals(
        "B4 53 CD 39 EA 09 DB C3 A4 FF 47 EB C8 BB BF B2".replace(" ", ""),
        VsdmUtils.bytesToHex(aesKey.getEncoded()).replace(" ", ""));
  }

  @Test
  void shouldThrowExceptionForNullKey() {
    val keyVersion = new VsdmKeyVersion('1', VsdmCheckDigitVersion.V1);
    assertThrows(NullPointerException.class, () -> new VsdmKey(null, keyVersion));
  }

  @Test
  void shouldEquals() {
    byte[] validKey = new byte[32];
    VsdmKey vsdmKey1 = new VsdmKey(validKey, new VsdmKeyVersion('2', VsdmCheckDigitVersion.V2));
    VsdmKey vsdmKey2 = new VsdmKey(validKey, new VsdmKeyVersion('2', VsdmCheckDigitVersion.V2));
    assertDoesNotThrow(() -> vsdmKey1.equals(vsdmKey2));
    assertDoesNotThrow(() -> vsdmKey1.equals(""));
    assertDoesNotThrow(() -> vsdmKey1.equals(vsdmKey1));

    assertNotEquals("", vsdmKey1);
    assertDoesNotThrow(vsdmKey1::hashCode);
    assertDoesNotThrow(vsdmKey1::toString);
  }
}
