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

package de.gematik.bbriccs.popp

import de.gematik.bbriccs.crypto.certificate.ProfessionOid
import de.gematik.bbriccs.smartcards.Egk
import de.gematik.bbriccs.smartcards.SmcB
import org.jose4j.lang.InvalidKeyException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.security.KeyStore
import java.util.Base64

class PoppTokenGeneratorTest {

  private val keyStorePath = "80276001011699900861-C_SMCB_AUT_E256_X509.p12"
  private val keyStorePassword = "00".toCharArray()

  private fun loadTestKeyStore(): KeyStore = KeyStore.getInstance("PKCS12").apply {
    val stream = requireNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream(keyStorePath)) {
      "Test keystore resource '$keyStorePath' is missing"
    }
    stream.use { load(it, keyStorePassword) }
  }

  private fun firstAlias(keyStore: KeyStore): String {
    val aliases = keyStore.aliases()
    require(aliases.hasMoreElements()) { "No alias found in test keystore" }
    return aliases.nextElement()
  }

  private fun configuration(
    keyAlias: String = firstAlias(loadTestKeyStore()),
    issuer: String = "https://test-issuer",
    kid: String = "test-kid",
  ): PoppTokenGeneratorConfiguration =
    PoppTokenGeneratorConfiguration(
      keyStorePath = keyStorePath,
      keyStorePassword = keyStorePassword,
      keyAlias = keyAlias,
      kid = kid,
      iss = issuer,
    )

  private fun request(issuer: String? = null, kid: String? = null) =
    PoppTokenGenerator.TokenGenerationRequest(
      patientId = "X123456789",
      insurerId = "109500969",
      actorId = "1-2-3",
      actorProfessionOid = "1.2.276.0.76.4.54",
      issuer = issuer,
      kid = kid,
    )

  private fun decodeHeader(jwt: String): String =
    String(Base64.getUrlDecoder().decode(jwt.split('.')[0]))

  private fun decodeClaims(jwt: String): String =
    String(Base64.getUrlDecoder().decode(jwt.split('.')[1]))

  @Test
  fun `from should throw meaningful error when configuration is null`() {
    val exception = assertThrows<IllegalArgumentException> {
      PoppTokenGenerator.from(null)
    }

    assertEquals("PoppTokenGeneratorConfiguration must not be null", exception.message)
  }

  @Test
  fun `sign should fail for non P-256 keystore key`() {
    val configuration = configuration()
    val generator = PoppTokenGenerator.from(configuration)
    val exception = assertThrows<InvalidKeyException> {
      generator.sign(request())
    }

    assertTrue(exception.message!!.contains("expects a key using P-256"))
  }

  @Test
  fun `signExpiredToken should fail for non P-256 keystore key`() {
    val generator = PoppTokenGenerator.from(configuration())
    val exception = assertThrows<InvalidKeyException> {
      generator.signExpiredToken(request())
    }

    assertTrue(exception.message!!.contains("expects a key using P-256"))
  }

  @Test
  fun `signWithWrongKey should still return jwt`() {
    val generator = PoppTokenGenerator.from(configuration())

    val jwt = generator.signWithWrongKey(request())

    assertTrue(jwt.isNotBlank())
    assertEquals(3, jwt.split('.').size)
  }

  @Test
  fun `signWithWrongKey should use configuration defaults for issuer and kid`() {
    val generator = PoppTokenGenerator.from(configuration(issuer = "https://cfg-issuer", kid = "cfg-kid"))

    val jwt = generator.signWithWrongKey(request())
    val header = decodeHeader(jwt)
    val claims = decodeClaims(jwt)

    assertTrue(header.contains("\"kid\":\"cfg-kid\""))
    assertTrue(claims.contains("\"iss\":\"https://cfg-issuer\""))
    assertTrue(claims.contains("\"proofMethod\":\"ehc-practitioner-1a\""))
    assertTrue(claims.contains("\"patientId\":\"X123456789\""))
  }

  @Test
  fun `signWithWrongKey should prefer request issuer and kid overrides`() {
    val generator = PoppTokenGenerator.from(configuration(issuer = "https://cfg-issuer", kid = "cfg-kid"))

    val jwt = generator.signWithWrongKey(request(issuer = "https://req-issuer", kid = "req-kid"))
    val header = decodeHeader(jwt)
    val claims = decodeClaims(jwt)

    assertTrue(header.contains("\"kid\":\"req-kid\""))
    assertTrue(claims.contains("\"iss\":\"https://req-issuer\""))
    assertFalse(claims.contains("\"iss\":\"https://cfg-issuer\""))
  }

  @Test
  fun `sign should fail when key alias does not exist`() {
    val generator = PoppTokenGenerator.from(configuration(keyAlias = "missing-alias"))

    val exception = assertThrows<IllegalStateException> {
      generator.sign(request())
    }

    assertEquals("No private key found for alias 'missing-alias'", exception.message)
  }

  @Test
  fun `tokenGenerationRequest with should map smcb and egk values`() {
    val smcB = mock(SmcB::class.java)
    val egk = mock(Egk::class.java)
    `when`(smcB.telematikId).thenReturn("1-2-3-4")
    `when`(smcB.profession).thenReturn(ProfessionOid.OEFFENTLICHE_APOTHEKE)
    `when`(egk.kvnr).thenReturn("X999999999")

    val request = PoppTokenGenerator.TokenGenerationRequest.with(smcB, egk)

    assertEquals("X999999999", request.patientId)
    assertEquals("109500969", request.insurerId)
    assertEquals("1-2-3-4", request.actorId)
    assertEquals(ProfessionOid.OEFFENTLICHE_APOTHEKE.value, request.actorProfessionOid)
    assertEquals("ehc-practitioner-1a", request.proofMethod)
  }
}
