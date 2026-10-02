/*
 * Copyright (Change Date see Readme) gematik GmbH
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

package de.gematik.bbriccs.popp

import de.gematik.bbriccs.utils.ResourceFileException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PoppTokenGeneratorConfigurationTest {

  @Test
  fun `loadKeyStore should load configured pkcs12 file`() {
    val configuration = PoppTokenGeneratorConfiguration(
      keyStorePath = "80276001011699900861-C_SMCB_AUT_E256_X509.p12",
      keyStorePassword = "00".toCharArray(),
      keyAlias = "unused",
      kid = "kid",
      iss = "iss",
    )

    val keyStore = configuration.loadKeyStore("PKCS12")

    assertTrue(keyStore.aliases().hasMoreElements())
  }

  @Test
  fun `loadKeyStore should fail for missing resource`() {
    val configuration = PoppTokenGeneratorConfiguration(
      keyStorePath = "missing-keystore.p12",
      keyStorePassword = "00".toCharArray(),
      keyAlias = "unused",
      kid = "kid",
      iss = "iss",
    )

    assertThrows<ResourceFileException> {
      configuration.loadKeyStore("PKCS12")
    }
  }
}
