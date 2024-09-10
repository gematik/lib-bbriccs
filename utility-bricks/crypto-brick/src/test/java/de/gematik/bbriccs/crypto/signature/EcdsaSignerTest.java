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

package de.gematik.bbriccs.crypto.signature;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.crypto.BC;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EcdsaSignerTest {

  private PrivateKey privateKey;
  private PublicKey publicKey;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    val kpg = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    val spec = new ECGenParameterSpec("secp256r1");
    kpg.initialize(spec);
    val kp = kpg.generateKeyPair();
    privateKey = kp.getPrivate();
    publicKey = kp.getPublic();
  }

  @Test
  void shouldGenerateSignature() {
    val signer = new EcdsaSigner();
    assertDoesNotThrow(() -> signer.sign(privateKey, "Test".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void shouldThrowOnVerify() {
    val signer = new EcdsaSigner();
    val data = "Test".getBytes(StandardCharsets.UTF_8);
    assertThrows(UnsupportedOperationException.class, () -> signer.verify(publicKey, data));
  }

  @Test
  void shouldThrowOnSigningWithInvalidKey() {
    val signer = new EcdsaSigner();
    val data = "Test".getBytes(StandardCharsets.UTF_8);
    val privateKey = mock(PrivateKey.class);

    assertThrows(InvalidKeyException.class, () -> signer.sign(privateKey, data));
  }
}
