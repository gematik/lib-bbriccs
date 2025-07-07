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

package de.gematik.bbriccs.crypto.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.crypto.BC;
import java.nio.charset.StandardCharsets;
import javax.crypto.AEADBadTagException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AesGcmTest {

  private static SecretKey symKey;

  @SneakyThrows
  @BeforeAll
  static void setup() {
    val keyGen = KeyGenerator.getInstance("AES", BC.getSecurityProvider());
    keyGen.init(128);
    symKey = keyGen.generateKey();
  }

  @Test
  void encryptAndDecrypt() {
    val aesGcm = new AesGcm(12, 16);
    val encryptedContent = aesGcm.encrypt(symKey, "Test".getBytes(StandardCharsets.UTF_8));
    val decrypted = aesGcm.decrypt(symKey, encryptedContent);
    assertEquals("Test", new String(decrypted, StandardCharsets.UTF_8));
  }

  @Test
  void shouldThrowOnDecrypt() {
    val aesGcm = new AesGcm(12, 16);
    val encryptedContent = aesGcm.encrypt(symKey, "Test".getBytes(StandardCharsets.UTF_8));
    val e = new String(encryptedContent) + "00";
    assertThrows(AEADBadTagException.class, () -> aesGcm.decrypt(symKey, e.getBytes()));
  }
}
