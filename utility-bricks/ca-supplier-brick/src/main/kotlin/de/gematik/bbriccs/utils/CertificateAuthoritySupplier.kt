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

import de.gematik.bbriccs.rest.HttpBClient
import de.gematik.bbriccs.rest.HttpBRequest
import de.gematik.bbriccs.rest.HttpRequestMethod
import de.gematik.bbriccs.rest.RestClient
import de.gematik.bbriccs.utils.dto.CertificateAuthorityDto
import de.gematik.bbriccs.utils.dto.RootCertificateAuthorityDto
import java.nio.charset.Charset

class CertificateAuthoritySupplier private constructor(private val environmentAnchor: TrustedEnvironmentAnchor, private val httpClient: HttpBClient) {
  fun getRootCAs(): RootCertificateAuthorityList {
    val path = environmentAnchor.getCaDownloadPath(CaType.ROOT_CA)
    val cas =
      downloadElementsFromBackend(path).map { name -> get("$path$name", true) as RootCertificateAuthorityDto }
        .toSet()
    return RootCertificateAuthorityList(cas)
  }

  private fun downloadElementsFromBackend(path: String): Set<String> =
    httpClient.send(HttpBRequest(HttpRequestMethod.GET, path)).let {
      """<a href="([^"]*)">""".toRegex().findAll(String(it.body, Charset.defaultCharset()))
        .map { matchResult -> matchResult.groupValues[1] }
        .filter { name -> name.endsWith(".der") }
        .toSet()
    }

  private fun get(path: String, isRootCa: Boolean): CertificateAuthorityDto =
    httpClient.send(HttpBRequest(HttpRequestMethod.GET, path)).let {
      return if (isRootCa) {
        RootCertificateAuthorityDto(it.body.inputStream().toCertificate(), path)
      } else {
        CertificateAuthorityDto(it.body.inputStream().toCertificate(), path)
      }
    }

  companion object {
    @JvmStatic
    fun builder() = Builder()
  }

  class Builder {
    private var environmentAnchor: TrustedEnvironmentAnchor = TiTrustedEnvironmentAnchor.TU
    private var useInternet = true
    private lateinit var httpClient: HttpBClient

    fun withEnvironmentAnchor(environmentAnchor: TrustedEnvironmentAnchor) = apply { this.environmentAnchor = environmentAnchor }

    fun useInternet() = apply { this.useInternet = true }

    fun useHttpClient(client: HttpBClient) = apply { this.httpClient = client }

    fun useTI() = apply { this.useInternet = false }

    fun getRootCAsFromBackend() = CertificateAuthoritySupplier(
      environmentAnchor,
      if (::httpClient.isInitialized) {
        httpClient
      } else {
        RestClient.forUrl(environmentAnchor.getUrl(useInternet)).withoutTlsVerification()
      },
    ).getRootCAs()
  }
}
