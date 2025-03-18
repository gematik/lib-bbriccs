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

import de.gematik.bbriccs.vsdm.exceptions.InvalidKeyLengthException;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;
import lombok.val;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public record VsdmKey(byte[] key, VsdmKeyVersion keyVersion) {

  public VsdmKey {
    if (key.length != 32) {
      throw new InvalidKeyLengthException(key, 32);
    }
  }

  public KeyParameter getKeyForVersion1() {
    return new KeyParameter(key);
  }

  /**
   * Derive an 128-Bit AES key from HMAC Key Requirement A_27286
   *
   * @return the derived AES key
   */
  public SecretKeySpec getKeyForVersion2() {
    val info = "VSDM+ Version 2 AES/GCM";

    // HKDF mit SHA-256, 16 Byte Output, ohne Salt
    val hkdf = new HKDFBytesGenerator(new SHA256Digest());
    val params = new HKDFParameters(key, null, info.getBytes());
    hkdf.init(params);

    // Derive the key
    byte[] aesKey = new byte[16];
    hkdf.generateBytes(aesKey, 0, aesKey.length);
    return new SecretKeySpec(aesKey, "AES");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VsdmKey vsdmKey = (VsdmKey) o;
    return Objects.deepEquals(key, vsdmKey.key) && Objects.equals(keyVersion, vsdmKey.keyVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(key), keyVersion);
  }

  @Override
  public String toString() {
    return "VsdmKey{" + "key=" + Arrays.toString(key) + ", keyVersion=" + keyVersion + '}';
  }
}
