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

import de.gematik.bbriccs.utils.dto.RootCertificateAuthorityDto
import java.security.cert.X509Certificate

class RootCertificateAuthorityList(private val internalList: Set<RootCertificateAuthorityDto>) :
  Set<RootCertificateAuthorityDto> by internalList {
  fun getRootCABy(subjectCN: String): RootCertificateAuthorityDto? {
    return internalList.find { !it.isCrossCa() && it.getSubjectCN() == subjectCN }
  }

  fun getRootCABy(ca: X509Certificate): RootCertificateAuthorityDto? {
    return getRootCABy(ca.getIssuerCN())
  }

  fun getRootCABy(cas: Collection<X509Certificate>): Set<RootCertificateAuthorityDto> {
    return cas.mapNotNull { getRootCABy(it) }.toSet()
  }

  fun minRootCA(cas: Collection<X509Certificate>) = getRootCABy(cas).minBy { it.getCaNumber() }
  fun maxRootCA(cas: Collection<X509Certificate>) = getRootCABy(cas).maxBy { it.getCaNumber() }

  fun nextRootCA(ca: RootCertificateAuthorityDto): RootCertificateAuthorityDto? =
    internalList.find {
      !it.isCrossCa() && it.getCaNumber() == ca.getCaNumber().plus(1)
    }

  fun beforeRootCA(ca: RootCertificateAuthorityDto): RootCertificateAuthorityDto? =
    internalList.find {
      !it.isCrossCa() && it.getCaNumber() == ca.getCaNumber().minus(1)
    }

  fun getCurrentCrossRootCAs(ca: RootCertificateAuthorityDto): Set<RootCertificateAuthorityDto> =
    internalList.filter {
      it.isCrossCa() && it.getIssuerCN() == ca.getSubjectCN()
    }.toSet()

  fun getChainOfCrossRootCAs(
    start: RootCertificateAuthorityDto,
    target: RootCertificateAuthorityDto,
  ): Set<RootCertificateAuthorityDto> {
    val ret = mutableSetOf<RootCertificateAuthorityDto>()
    val next: (RootCertificateAuthorityDto) -> RootCertificateAuthorityDto? =
      if (start < target) { ca -> nextRootCA(ca) } else { ca -> beforeRootCA(ca) }
    var current = start
    while (next(current) != null && current != target) {
      next(current)?.let { nextCa ->
        ret.addAll(getCurrentCrossRootCAs(current).filter { it.getSubjectCN() == nextCa.getSubjectCN() })
        current = nextCa
      }
    }
    return ret
  }

  fun getChainOfCrossRootCAs(cas: Collection<X509Certificate>, current: RootCertificateAuthorityDto): Set<RootCertificateAuthorityDto> {
    if (cas.isEmpty()) {
      return setOf()
    }
    val min = minRootCA(cas)
    val max = maxRootCA(cas)
    return getChainOfCrossRootCAs(current, min).union(getChainOfCrossRootCAs(current, max))
  }
}
