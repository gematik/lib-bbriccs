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

import de.gematik.bbriccs.utils.exceptions.MissingCertificateAuthorityIssuer
import de.gematik.bbriccs.utils.exceptions.MissingCertificateAuthoritySubject
import java.security.cert.X509Certificate
import javax.naming.ldap.LdapName

open class CertificateAuthorityDto(val cert: X509Certificate, val url: String) {
  fun getSubjectCN(): String {
    val ldapDN = LdapName(this.cert.subjectX500Principal.name)
    for (rdn in ldapDN.rdns) {
      if (rdn.type.equals("CN", ignoreCase = true)) {
        return rdn.value.toString()
      }
    }
    throw MissingCertificateAuthoritySubject(this.cert)
  }

  fun getIssuerCN(): String {
    val issuerX500Principal = this.cert.issuerX500Principal.name
    val ldapDN = LdapName(issuerX500Principal)
    for (rdn in ldapDN.rdns) {
      if (rdn.type.equals("CN", ignoreCase = true)) {
        return rdn.value.toString()
      }
    }
    throw MissingCertificateAuthorityIssuer(this.cert)
  }

  override fun toString() = "Issuer: ${this.getIssuerCN()} -> Subject: ${this.getSubjectCN()}"
}
