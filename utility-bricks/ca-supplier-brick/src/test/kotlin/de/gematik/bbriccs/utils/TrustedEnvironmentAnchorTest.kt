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

package de.gematik.bbriccs.utils

import de.gematik.bbriccs.crypto.CryptographySpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrustedEnvironmentAnchorTest {

  @Test
  fun `getCaDownloadPath returns correct path for ROOT_CA with ECC`() {
    val expectedPath = "/ECC/ROOT-CA/"
    assertEquals(expectedPath, TiTrustedEnvironmentAnchor.TU.getCaDownloadPath(CaType.ROOT_CA, CryptographySpecification.ECC))
  }

  @Test
  fun `getCaDownloadPath returns correct path for SUB_CA with RSA`() {
    val expectedPath = "/RSA/SUB-CA/"
    assertEquals(expectedPath, TiTrustedEnvironmentAnchor.RU.getCaDownloadPath(CaType.SUB_CA, CryptographySpecification.RSA))
  }

  @Test
  fun `getUrl returns internet URL when useInternet is true`() {
    val expectedUrl = "https://download-test.tsl.ti-dienste.de"
    assertEquals(expectedUrl, TiTrustedEnvironmentAnchor.TU.getUrl(true))
  }

  @Test
  fun `getUrl returns TI URL when useInternet is false`() {
    val expectedUrl = "http://download.tsl.telematik"
    assertEquals(expectedUrl, TiTrustedEnvironmentAnchor.PU.getUrl(false))
  }
}
