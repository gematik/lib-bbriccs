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

package de.gematik.bbriccs.crypto;

import java.security.Provider;
import java.security.Security;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BC {
  private static final Provider SECURITY_PROVIDER = new BouncyCastleProvider();

  private static boolean initialized;

  static {
    init();
  }

  private BC() {}

  public static void init() {
    if (!initialized) {
      Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
      Security.insertProviderAt(SECURITY_PROVIDER, 1);
      // this allows BC to read RSA certificates with invalid padding which occasionally occurs in
      // some virtual smartcards
      // see:
      // https://stackoverflow.com/questions/46979405/java-get-key-failed-java-security-invalidkeyexception-invalid-rsa-private-ke
      // https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/crypto/params/RSAKeyParameters.java#L80-L85
      Security.setProperty("org.bouncycastle.rsa.allow_unsafe_mod", "true");
      initialized = true;
    }
  }

  public static Provider getSecurityProvider() {
    return SECURITY_PROVIDER;
  }

  @SneakyThrows
  public static SecretKey generateAESDecryptionKey(int keySize) {
    val keyGenerator = KeyGenerator.getInstance("AES", BC.getSecurityProvider());
    keyGenerator.init(keySize);
    return keyGenerator.generateKey();
  }
}
