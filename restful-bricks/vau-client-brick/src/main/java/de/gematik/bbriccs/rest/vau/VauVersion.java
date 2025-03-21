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

package de.gematik.bbriccs.rest.vau;

import de.gematik.bbriccs.crypto.encryption.AesGcm;
import de.gematik.bbriccs.crypto.encryption.Ecies;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VauVersion {
  V1(1, 16, 128, 3 + 16 * 2, 12, "brainpoolP256r1");

  private final int versionId;
  private final int reqIdSize;
  private final int keySize;
  private final int respSize;
  private final int ivSize;
  private final String curve;

  public char getVersionForInnerHttp() {
    return Character.forDigit(versionId, 10);
  }

  public byte getVersionForEncryption() {
    return (byte) versionId;
  }

  public AesGcm getSymmetricMethod() {
    return new AesGcm(this.getIvSize(), 16);
  }

  public Ecies getAsymmetricMethod() {
    return new Ecies(
        this.getVersionForEncryption(),
        "ecies-vau-transport".getBytes(StandardCharsets.UTF_8),
        this.getIvSize(),
        16,
        this.getCurve());
  }
}
