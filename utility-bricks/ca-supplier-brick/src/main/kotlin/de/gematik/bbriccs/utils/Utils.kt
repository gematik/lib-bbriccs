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

package de.gematik.bbriccs.utils

import de.gematik.bbriccs.utils.exceptions.MissingCertificateAuthoritySubject
import java.io.InputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.naming.ldap.LdapName
import javax.security.auth.x500.X500Principal

fun InputStream.toCertificate() = CertificateFactory.getInstance("X.509").generateCertificate(this) as X509Certificate

private fun X509Certificate.getCN(prim: X500Principal): String {
  val ldapDN = LdapName(prim.name)
  for (rdn in ldapDN.rdns) {
    if (rdn.type.equals("CN", ignoreCase = true)) {
      return rdn.value.toString()
    }
  }
  throw MissingCertificateAuthoritySubject(this)
}

fun X509Certificate.getSubjectCN() = getCN(this.subjectX500Principal)

fun X509Certificate.getIssuerCN() = getCN(this.issuerX500Principal)
