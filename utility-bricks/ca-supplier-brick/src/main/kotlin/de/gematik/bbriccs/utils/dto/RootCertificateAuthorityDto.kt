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

package de.gematik.bbriccs.utils.dto

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import de.gematik.bbriccs.utils.exceptions.MissingRootCertificateAuthorityNumber
import de.gematik.bbriccs.utils.toCertificate
import java.io.IOException
import java.security.cert.X509Certificate

class RootCertificateAuthorityDto(cert: X509Certificate, val nextCrossCA: X509Certificate? = null, val prevCrossCA: X509Certificate? = null) :
  CertificateAuthorityDto(cert), Comparable<RootCertificateAuthorityDto> {
  override fun compareTo(other: RootCertificateAuthorityDto): Int {
    return this.getCaNumber().compareTo(other.getCaNumber())
  }

  fun isCrossCa(): Boolean = getSubjectCN() != getIssuerCN()

  fun getCaNumber(): Int =
    getSubjectCN().filter(Char::isDigit).let {
      if (it.isEmpty()) throw MissingRootCertificateAuthorityNumber(cert) else it.toInt()
    }
}

class RootCASerializer : JsonDeserializer<RootCertificateAuthorityDto>() {

  @Throws(IOException::class, JsonProcessingException::class)
  override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): RootCertificateAuthorityDto {
    val node: JsonNode = jp.codec.readTree(jp)
    return RootCertificateAuthorityDto(
      node["cert"].asText().toCertificate(),
      node["next"].asText().takeIf(String::isNotEmpty)?.toCertificate(),
      node["prev"].asText().takeIf(String::isNotEmpty)?.toCertificate(),
    )
  }
}
