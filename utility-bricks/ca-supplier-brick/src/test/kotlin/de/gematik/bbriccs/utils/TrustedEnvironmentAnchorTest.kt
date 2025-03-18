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

package de.gematik.bbriccs.utils

import de.gematik.bbriccs.crypto.CryptographySpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TrustedEnvironmentAnchorTest {

  @Test
  fun `getCaDownloadPath returns correct path for ROOT_CA with ECC`() {
    val expectedPath = "/ECC/ROOT-CA/roots.json"
    assertEquals(expectedPath, TiTrustedEnvironmentAnchor.TU.getCaDownloadPath(CaType.ROOT_CA, CryptographySpecification.ECC))
  }

  @Test
  fun `getCaDownloadPath returns correct path for SUB_CA with RSA`() {
    val expectedPath = "/RSA/SUB-CA/"
    assertEquals(expectedPath, TiTrustedEnvironmentAnchor.RU.getCaDownloadPath(CaType.SUB_CA, CryptographySpecification.RSA))
  }

  @ParameterizedTest
  @EnumSource(TiTrustedEnvironmentAnchor::class)
  fun `getUrl does not throw exceptions`(anchor: TiTrustedEnvironmentAnchor) {
    assertDoesNotThrow { anchor.getUrl(true) }
    assertDoesNotThrow { anchor.getUrl(false) }
  }

  @Test
  fun `getUrl returns TI URL when useInternet is false`() {
    assertEquals("http://download.tsl.telematik", TiTrustedEnvironmentAnchor.PU.getUrl(false))
    assertEquals("https://download.tsl.ti-dienste.de", TiTrustedEnvironmentAnchor.PU.getUrl(true))
  }
}
