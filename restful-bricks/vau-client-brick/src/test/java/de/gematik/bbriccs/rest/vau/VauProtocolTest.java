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

package de.gematik.bbriccs.rest.vau;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.crypto.BC;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPublicKey;
import javax.crypto.SecretKey;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class VauProtocolTest {

  @Test
  @Disabled("does not work yet")
  void shouldThrowOnInvalidEncryptionKeys() {
    BC.init();

    val publicKey = mock(ECPublicKey.class);
    val symmetricKey = mock(SecretKey.class);
    when(symmetricKey.getEncoded()).thenReturn("Hello, World!".getBytes(StandardCharsets.UTF_8));
    val originalSecurityProvider = BC.getSecurityProvider();
    val vau = new VauProtocol(VauVersion.V1, publicKey);

    try (val mockedBC = mockStatic(BC.class)) {
      mockedBC.when(() -> BC.generateAESDecryptionKey(anyInt())).thenReturn(symmetricKey);
      mockedBC.when(BC::getSecurityProvider).thenReturn(originalSecurityProvider);

      val d = vau.encrypt("ACCESS_CODE", "innerHttp".getBytes(StandardCharsets.UTF_8));
    }
  }
}
