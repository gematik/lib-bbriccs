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

import de.gematik.bbriccs.utils.exceptions.MissingCertificateAuthorityIssuer
import de.gematik.bbriccs.utils.exceptions.MissingCertificateAuthoritySubject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.security.auth.x500.X500Principal

class CertificateAuthorityDtoTest {
  private lateinit var certificate: X509Certificate
  private lateinit var certificateAuthorityDto: CertificateAuthorityDto

  @BeforeEach
  fun setup() {
    certificate = mock(X509Certificate::class.java)
    certificateAuthorityDto = CertificateAuthorityDto(certificate)
  }

  @Test
  fun `getIssuerCN should return correct Subject CN and Issuer CN`() {
    `when`(certificate.subjectX500Principal).thenReturn(X500Principal("CN=Test Subject"))
    `when`(certificate.issuerX500Principal).thenReturn(X500Principal("CN=Test Issuer"))
    assertEquals("Test Issuer", certificateAuthorityDto.getIssuerCN())
    assertEquals("Test Subject", certificateAuthorityDto.getSubjectCN())

    assertDoesNotThrow { certificateAuthorityDto.toString() }
  }

  @Test
  fun `getSubjectCN should throw MissingCertificateAuthoritySubject when CN is missing`() {
    `when`(certificate.subjectX500Principal).thenReturn(X500Principal("O=Test Organization"))
    assertThrows(MissingCertificateAuthoritySubject::class.java) {
      certificateAuthorityDto.getSubjectCN()
    }
  }

  @Test
  fun `getIssuerCN should throw MissingCertificateAuthorityIssuer when CN is missing`() {
    `when`(certificate.issuerX500Principal).thenReturn(X500Principal("O=Test Organization"))
    assertThrows(MissingCertificateAuthorityIssuer::class.java) {
      certificateAuthorityDto.getIssuerCN()
    }
  }

  @Test
  fun `NotValidBefore and NotValidAfter do not throw exceptions`() {
    val notValidBefore = LocalDate.of(2024, 1, 1)
    val notValidAfter = LocalDate.of(2025, 1, 1)

    `when`(certificate.notBefore).thenReturn(Date.from(notValidBefore.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    `when`(certificate.notAfter).thenReturn(Date.from(notValidAfter.atStartOfDay(ZoneId.systemDefault()).toInstant()))

    assertEquals(notValidBefore, certificateAuthorityDto.getNotValidBefore())
    assertEquals(notValidAfter, certificateAuthorityDto.getNotValidAfter())
  }
}
