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

import de.gematik.bbriccs.smartcards.Egk
import de.gematik.bbriccs.smartcards.SmcB
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit

class PoppTokenGenerator private constructor(
  private val configuration: PoppTokenGeneratorConfiguration,
  private val keyStoreLoader: () -> KeyStore,
) {
  private val signingMaterial: SigningMaterial by lazy { loadSigningMaterial() }

  private fun sign(request: TokenGenerationRequest, iat: Instant = Instant.now(), key: PrivateKey = signingMaterial.privateKey): String {
    val token = createToken(request = request, request.kid ?: configuration.kid, request.issuer ?: configuration.iss, iat.epochSecond)
    return token.toJwt(key, signingMaterial.certificateChain)
  }

  fun sign(request: TokenGenerationRequest): String {
    return sign(request = request, key = signingMaterial.privateKey)
  }

  fun signExpiredToken(request: TokenGenerationRequest): String {
    val expiredIat = Instant.now().minus(30, ChronoUnit.MINUTES)
    return sign(request, expiredIat)
  }

  fun signWithWrongKey(request: TokenGenerationRequest): String {
    val wrongPrivateKey = KeyPairGenerator.getInstance("EC").apply {
      initialize(256)
    }.generateKeyPair().private
    return sign(request, key = wrongPrivateKey)
  }

  private fun createToken(request: TokenGenerationRequest, kid: String, issuer: String, iat: Long): PoppToken =
    PoppToken(
      header = PoppToken.TokenHeader(kid = kid),
      claims = PoppToken.TokenClaims(
        proofMethod = request.proofMethod,
        patientProofTime = iat,
        iat = iat,
        patientId = request.patientId,
        insurerId = request.insurerId,
        actorId = request.actorId,
        actorProfessionOid = request.actorProfessionOid,
        iss = issuer,
      ),
    )

  private fun loadSigningMaterial(): SigningMaterial {
    val keyStore = keyStoreLoader()
    val key = keyStore.getKey(configuration.keyAlias, configuration.keyStorePassword)
      ?: error("No private key found for alias '${configuration.keyAlias}'")
    val privateKey = key as? PrivateKey
      ?: error("Alias '${configuration.keyAlias}' does not contain a private key")

    val certificateChain = keyStore.getCertificateChain(configuration.keyAlias)
      ?.mapNotNull { it as? X509Certificate }
      .orEmpty()
      .ifEmpty {
        listOfNotNull(keyStore.getCertificate(configuration.keyAlias) as? X509Certificate)
      }

    require(certificateChain.isNotEmpty()) {
      "No X509 certificate found for alias '${configuration.keyAlias}'"
    }

    return SigningMaterial(
      privateKey = privateKey,
      certificateChain = certificateChain,
    )
  }

  private data class SigningMaterial(
    val privateKey: PrivateKey,
    val certificateChain: List<X509Certificate>,
  )

  data class TokenGenerationRequest(
    var proofMethod: String = "ehc-practitioner-1a",
    val patientId: String,
    val insurerId: String,
    val actorId: String,
    val actorProfessionOid: String,
    var issuer: String? = null,
    var kid: String? = null,
  ) {
    companion object {
      @JvmStatic fun with(smcB: SmcB, egk: Egk) = TokenGenerationRequest(
        patientId = egk.kvnr,
        insurerId = "109500969",
        actorId = smcB.telematikId,
        actorProfessionOid = smcB.profession.value,
      )
    }
  }

  companion object {
    @JvmStatic
    fun from(configuration: PoppTokenGeneratorConfiguration?): PoppTokenGenerator {
      val safeConfiguration = requireNotNull(configuration) {
        "PoppTokenGeneratorConfiguration must not be null"
      }

      return PoppTokenGenerator(safeConfiguration) { safeConfiguration.loadKeyStore() }
    }
  }
}
