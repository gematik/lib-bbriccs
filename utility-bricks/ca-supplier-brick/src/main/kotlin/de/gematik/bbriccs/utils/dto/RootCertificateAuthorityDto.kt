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
import java.security.cert.X509Certificate

class RootCertificateAuthorityDto(cert: X509Certificate, url: String) :
  CertificateAuthorityDto(cert, url), Comparable<RootCertificateAuthorityDto> {
  override fun compareTo(other: RootCertificateAuthorityDto): Int {
    return this.getCaNumber().compareTo(other.getCaNumber())
  }

  fun isCrossCa(): Boolean = url.contains("-CROSS-")

  fun getCaNumber(): Int =
    getSubjectCN().filter(Char::isDigit).let {
      if (it.isEmpty()) throw MissingRootCertificateAuthorityNumber(cert) else it.toInt()
    }
}
