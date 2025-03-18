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
  private lateinit var rootCertificateAuthorityDto: RootCertificateAuthorityDto
  private lateinit var crossCertificateAuthorityDto: RootCertificateAuthorityDto

  @BeforeEach
  fun setup() {
    val rootCA = mock(X509Certificate::class.java)
    `when`(rootCA.subjectX500Principal).thenReturn(X500Principal("CN=GEM.RCA1"))
    `when`(rootCA.issuerX500Principal).thenReturn(X500Principal("CN=GEM.RCA1"))

    val crossCA = mock(X509Certificate::class.java)
    `when`(crossCA.subjectX500Principal).thenReturn(X500Principal("CN=GEM.RCA2"))
    `when`(crossCA.issuerX500Principal).thenReturn(X500Principal("CN=GEM.RCA1"))

    rootCertificateAuthorityDto = RootCertificateAuthorityDto(rootCA)
    crossCertificateAuthorityDto = RootCertificateAuthorityDto(crossCA)
  }

  @Test
  fun `isCrossCa should return false when subject and issuer are equals`() {
    assertFalse(rootCertificateAuthorityDto.isCrossCa())
  }

  @Test
  fun `isCrossCa should return true when subject and issuer are not equals`() {
    assertTrue(crossCertificateAuthorityDto.isCrossCa())
  }

  @Test
  fun `getCaNumber should return correct number when CN contains digits`() {
    val caNumber = rootCertificateAuthorityDto.getCaNumber()
    assertEquals(1, caNumber)
  }

  @Test
  fun `getCaNumber should return 0 when CN does not contain digits`() {
    `when`(rootCertificateAuthorityDto.cert.subjectX500Principal).thenReturn(X500Principal("CN=TestSubject"))
    assertThrows(MissingRootCertificateAuthorityNumber::class.java) { rootCertificateAuthorityDto.getCaNumber() }
  }
}
