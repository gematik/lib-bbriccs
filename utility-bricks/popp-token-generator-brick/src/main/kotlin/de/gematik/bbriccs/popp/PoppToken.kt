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
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.util.*

/**
 * Reine Erzeugungslogik für PoPP-Token.
 *
 * Erwartet für ES256 einen EC-Schlüssel auf P-256 / secp256r1.
 * Die Zeitwerte [TokenClaims.iat] und [TokenClaims.patientProofTime] werden als Epoch-Sekunden erwartet.
 */
data class PoppToken(
  val header: TokenHeader,
  val claims: TokenClaims,
) {
  fun toJwt(privateKey: PrivateKey, certificateChain: List<X509Certificate>): String {
    val tokenHeader = header

    require(header.alg == AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256) {
      "Only ES256 is supported by this implementation."
    }
    require(privateKey is ECPrivateKey) {
      "ES256 requires an ECPrivateKey, but got: ${privateKey.algorithm}."
    }
    require(certificateChain.isNotEmpty()) {
      "At least one X509 certificate is required for the x5c header."
    }

    val x5c = certificateChain.map { cert ->
      Base64.getEncoder().encodeToString(cert.encoded)
    }

    val jwtClaims = JwtClaims().apply {
      issuedAt = NumericDate.fromSeconds(claims.iat)
      setClaim(CLAIM_VERSION, claims.version)
      issuer = claims.iss
      setClaim(CLAIM_PROOF_METHOD, claims.proofMethod)
      setClaim(CLAIM_PATIENT_PROOF_TIME, claims.patientProofTime)
      setClaim(CLAIM_PATIENT_ID, claims.patientId)
      setClaim(CLAIM_INSURER_ID, claims.insurerId)
      setClaim(CLAIM_ACTOR_ID, claims.actorId)
      setClaim(CLAIM_ACTOR_PROFESSION_OID, claims.actorProfessionOid)
    }

    return JsonWebSignature().apply {
      payload = jwtClaims.toJson()
      key = privateKey
      algorithmHeaderValue = tokenHeader.alg
      setHeader(HEADER_TYP, tokenHeader.typ)
      setHeader(HEADER_KID, tokenHeader.kid)
      setHeader(HEADER_X5C, x5c)
    }.compactSerialization
  }

  data class TokenHeader(
    val kid: String,
    val typ: String = POPP_TYP,
    val alg: String = AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
  )

  data class TokenClaims(
    val proofMethod: String,
    val patientProofTime: Long,
    val iat: Long,
    val patientId: String,
    val insurerId: String,
    val actorId: String,
    val actorProfessionOid: String,
    val version: String = POPP_VERSION,
    val iss: String,
  )

  companion object {
    const val POPP_TYP: String = "vnd.telematik.popp+jwt"
    const val POPP_VERSION: String = "1.0.0"

    private const val HEADER_TYP = "typ"
    private const val HEADER_KID = "kid"
    private const val HEADER_X5C = "x5c"

    private const val CLAIM_VERSION = "version"
    private const val CLAIM_PROOF_METHOD = "proofMethod"
    private const val CLAIM_PATIENT_PROOF_TIME = "patientProofTime"
    private const val CLAIM_PATIENT_ID = "patientId"
    private const val CLAIM_INSURER_ID = "insurerId"
    private const val CLAIM_ACTOR_ID = "actorId"
    private const val CLAIM_ACTOR_PROFESSION_OID = "actorProfessionOid"
  }
}
