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

package de.gematik.bbriccs.crypto.signature;

import de.gematik.bbriccs.crypto.BC;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RsaPssSignerTest {

  private PrivateKey privateKey;
  private PublicKey publicKey;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    val kpg = KeyPairGenerator.getInstance("RSA", BC.getSecurityProvider());
    kpg.initialize(2048);
    val kp = kpg.generateKeyPair();
    privateKey = kp.getPrivate();
    publicKey = kp.getPublic();
  }

  @Test
  void shouldGenerateSignature() {
    val signer = RsaPssSigner.sha256withMgf1();
    Assertions.assertDoesNotThrow(
        () -> signer.sign(privateKey, "Test".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void validSignatureLength() {
    val signer = RsaPssSigner.sha256withMgf1();
    val sign = signer.sign(privateKey, "Test".getBytes(StandardCharsets.UTF_8));
    Assertions.assertEquals(256, sign.length);
  }

  @Test
  void signatureValidationNotSupported() {
    val signer = RsaPssSigner.sha256withMgf1();
    val data = "Test".getBytes(StandardCharsets.UTF_8);
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> signer.verify(publicKey, data));
  }
}
