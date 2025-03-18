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

package de.gematik.bbriccs.utils

import de.gematik.bbriccs.utils.dto.RootCertificateAuthorityDto
import java.security.cert.X509Certificate
import java.util.*

class RootCertificateAuthorityList(private val internalList: Set<RootCertificateAuthorityDto>) :
  Set<RootCertificateAuthorityDto> by internalList {
  fun getRootCABy(subjectCN: String): RootCertificateAuthorityDto? = internalList.find { !it.isCrossCa() && it.getSubjectCN() == subjectCN }

  fun getRootCAByCompCA(crossCA: X509Certificate) = getRootCABy(crossCA.getIssuerCN())
  fun getRootCAByCrossCA(crossCA: X509Certificate) = getRootCABy(crossCA.getSubjectCN())

  fun nextRootCA(rootCa: RootCertificateAuthorityDto): RootCertificateAuthorityDto? = rootCa.nextCrossCA?.let { getRootCAByCrossCA(it) }
  fun prevRootCA(rootCa: RootCertificateAuthorityDto): RootCertificateAuthorityDto? = rootCa.prevCrossCA?.let { getRootCAByCrossCA(it) }

  fun getChainOfCrossRootCAsBetween(
    start: RootCertificateAuthorityDto,
    target: RootCertificateAuthorityDto,
  ): LinkedList<X509Certificate> {
    val ret = LinkedList<X509Certificate>()

    val nextCrossCa: (RootCertificateAuthorityDto) -> X509Certificate? =
      if (start < target) {
        {
            ca ->
          ca.nextCrossCA
        }
      } else {
        {
            ca ->
          ca.prevCrossCA
        }
      }

    var current: RootCertificateAuthorityDto? = start
    while (current != null && current != target) {
      current = nextCrossCa(current)?.let {
        ret.add(it)
        getRootCAByCrossCA(it)
      }
    }
    return ret
  }

  fun getChainOfCrossRootCAByCompCAs(cas: Collection<X509Certificate>, current: RootCertificateAuthorityDto): LinkedList<X509Certificate> {
    if (cas.isEmpty()) {
      return LinkedList()
    }
    val min = cas.mapNotNull { getRootCAByCompCA(it) }.minBy { it.getCaNumber() }
    val max = cas.mapNotNull { getRootCAByCompCA(it) }.maxBy { it.getCaNumber() }
    return LinkedList<X509Certificate>().apply {
      addAll(getChainOfCrossRootCAsBetween(current, min))
      addAll(getChainOfCrossRootCAsBetween(current, max))
    }
  }
}
