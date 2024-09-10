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

package de.gematik.bbriccs.utils.dto

import de.gematik.bbriccs.utils.exceptions.MissingRootCertificateAuthorityNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

class RootCertificateAuthorityDtoTest {
  private lateinit var certificate: X509Certificate
  private lateinit var rootCertificateAuthorityDto: RootCertificateAuthorityDto

  @BeforeEach
  fun setup() {
    certificate = mock(X509Certificate::class.java)
    rootCertificateAuthorityDto = RootCertificateAuthorityDto(certificate, "http://test.url")
  }

  @Test
  fun `isCrossCa should return true when url contains CROSS`() {
    rootCertificateAuthorityDto = RootCertificateAuthorityDto(certificate, "http://GEM.RCA3_TEST-ONLY-CROSS-GEM.RCA4_TEST-ONLY.der")
    val isCrossCa = rootCertificateAuthorityDto.isCrossCa()
    assertTrue(isCrossCa)
  }

  @Test
  fun `isCrossCa should return false when url does not contain CROSS`() {
    val isCrossCa = rootCertificateAuthorityDto.isCrossCa()
    assertFalse(isCrossCa)
  }

  @Test
  fun `getCaNumber should return correct number when CN contains digits`() {
    `when`(certificate.subjectX500Principal).thenReturn(X500Principal("CN=GEM.RCA7 TEST-ONLY"))
    val caNumber = rootCertificateAuthorityDto.getCaNumber()
    assertEquals(7, caNumber)
  }

  @Test
  fun `getCaNumber should return 0 when CN does not contain digits`() {
    `when`(certificate.subjectX500Principal).thenReturn(X500Principal("CN=TestSubject"))
    assertThrows(MissingRootCertificateAuthorityNumber::class.java) { rootCertificateAuthorityDto.getCaNumber() }
  }
}
