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

import org.jose4j.jws.AlgorithmIdentifiers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.KeyPairGenerator

class PoppTokenTest {

  private fun tokenWithHeader(alg: String = AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256): PoppToken =
    PoppToken(
      header = PoppToken.TokenHeader(kid = "kid-1", alg = alg),
      claims = PoppToken.TokenClaims(
        proofMethod = "ehc-practitioner-1a",
        patientProofTime = 1234,
        iat = 1234,
        patientId = "X123456789",
        insurerId = "109500969",
        actorId = "1-2-3",
        actorProfessionOid = "1.2.276.0.76.4.54",
        iss = "https://issuer",
      ),
    )

  @Test
  fun `toJwt should reject unsupported algorithm`() {
    val privateKey = KeyPairGenerator.getInstance("RSA").generateKeyPair().private

    val ex = assertThrows<IllegalArgumentException> {
      tokenWithHeader(alg = "RS256").toJwt(privateKey, emptyList())
    }

    assertEquals("Only ES256 is supported by this implementation.", ex.message)
  }

  @Test
  fun `toJwt should reject non EC private keys for ES256`() {
    val privateKey = KeyPairGenerator.getInstance("RSA").generateKeyPair().private

    val ex = assertThrows<IllegalArgumentException> {
      tokenWithHeader().toJwt(privateKey, emptyList())
    }

    assertEquals("ES256 requires an ECPrivateKey, but got: RSA.", ex.message)
  }

  @Test
  fun `toJwt should require certificate chain`() {
    val privateKey = KeyPairGenerator.getInstance("EC").apply { initialize(256) }.generateKeyPair().private

    val ex = assertThrows<IllegalArgumentException> {
      tokenWithHeader().toJwt(privateKey, emptyList())
    }

    assertEquals("At least one X509 certificate is required for the x5c header.", ex.message)
  }
}
