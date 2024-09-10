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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import de.gematik.bbriccs.rest.HttpBClient
import de.gematik.bbriccs.rest.RestClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.FileNotFoundException

class CertificateAuthoritySupplierTest {

  private lateinit var rootCAs: RootCertificateAuthorityList
  private lateinit var wireMockServer: WireMockServer
  private lateinit var httpBClient: HttpBClient

  @BeforeEach
  fun setup() {
    // Start WireMock server
    wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    wireMockServer.start()

    httpBClient = RestClient.forUrl("http://localhost:${wireMockServer.port()}").withoutTlsVerification()

    CaType.entries.forEach {
      wireMockServer.stubFor(
        get(urlEqualTo("/ECC/$it/"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Certs.getRootCAs().joinToString(separator = " ") { it.href }),
          ),
      )

      Certs.getRootCAs().forEach { cert ->
        wireMockServer.stubFor(
          get(urlEqualTo("/ECC/$it/${cert.caName}.der"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(cert.cert.encoded),
            ),
        )
      }
    }

    rootCAs = CertificateAuthoritySupplier.builder().useHttpClient(httpBClient).getRootCAsFromBackend()
  }

  @AfterEach
  fun tearDown() {
    wireMockServer.stop()
  }

  @Test
  fun `getRootCAs should return correct RootCertificateAuthorityList`() {
    rootCAs.forEach { ca ->
      assertDoesNotThrow { ca.getSubjectCN() }
      assertDoesNotThrow { ca.getIssuerCN() }
      assertDoesNotThrow { ca.getCaNumber() }
      assertDoesNotThrow { ca.isCrossCa() }
      assertNotNull(ca.cert)
    }
  }

  @Test
  fun `getSubCAs should return correct list of CertificateAuthorityDto`() {
    rootCAs.forEach { ca ->
      assertDoesNotThrow { ca.getSubjectCN() }
      assertDoesNotThrow { ca.getIssuerCN() }
      assertNotNull(ca.cert)
    }
  }

  @Test
  fun `Navigation RootCertificateAuthorityList`() {
    val ca5 = rootCAs.getRootCABy(Certs.CA5.subjectCN) ?: throw AssertionError("CA5 not found")
    val ca6 = rootCAs.getRootCABy(Certs.CA6.subjectCN) ?: throw AssertionError("CA6 not found")
    val ca62Ca5 = rootCAs.getCurrentCrossRootCAs(ca5).first()
    val ca52Ca6 = rootCAs.getCurrentCrossRootCAs(ca6).first()

    assertEquals(ca6, rootCAs.nextRootCA(ca5))
    assertEquals(ca5, rootCAs.beforeRootCA(ca6))
    rootCAs.getChainOfCrossRootCAs(ca5, ca6).let {
      assertEquals(1, it.size)
      assertEquals(ca62Ca5, it.first())
    }

    rootCAs.getChainOfCrossRootCAs(ca6, ca5).let {
      assertEquals(1, it.size)
      assertEquals(ca52Ca6, it.first())
    }

    rootCAs.getChainOfCrossRootCAs(listOf(Certs.KOMP_CA51.cert, Certs.KOMP_CA54.cert), ca5).let {
      assertEquals(1, it.size)
    }
  }

  @Test
  fun `Builder should not throw exceptions`() {
    assertDoesNotThrow { CertificateAuthoritySupplier.Builder().useTI() }
    assertDoesNotThrow { CertificateAuthoritySupplier.Builder().useInternet() }
    assertDoesNotThrow { CertificateAuthoritySupplier.Builder().withEnvironmentAnchor(TiTrustedEnvironmentAnchor.RU) }
  }

  @ParameterizedTest
  @Disabled
  @EnumSource(TiTrustedEnvironmentAnchor::class)
  fun `Integration Test`(anchor: TrustedEnvironmentAnchor) {
    val rootCAs = CertificateAuthoritySupplier.Builder().withEnvironmentAnchor(anchor).getRootCAsFromBackend()
    rootCAs.forEach { ca ->
      assertDoesNotThrow { ca.getSubjectCN() }
      assertDoesNotThrow { ca.getIssuerCN() }
      assertDoesNotThrow { ca.getCaNumber() }
      assertDoesNotThrow { ca.isCrossCa() }
      assertNotNull(ca.cert)
    }
  }
}

enum class Certs(val caName: String, val isRootCA: Boolean = true) {
  CA5("GEM.RCA5_TEST-ONLY"),
  CA6("GEM.RCA6_TEST-ONLY"),
  CA6_CROSS_CA5("GEM.RCA6_TEST-ONLY-CROSS-GEM.RCA5_TEST-ONLY"),
  CA5_CROSS_CA6("GEM.RCA5_TEST-ONLY-CROSS-GEM.RCA6_TEST-ONLY"),
  KOMP_CA51("GEM.KOMP-CA51_TEST-ONLY", false),
  KOMP_CA54("GEM.KOMP-CA54_TEST-ONLY", false),
  ;

  val cert = javaClass.classLoader.getResourceAsStream("${this.caName}.der")?.toCertificate()
    ?: throw FileNotFoundException("$caName not found in resources folder")

  val href = "<a href=\"$caName.der\">"

  val subjectCN = caName.replace("_", " ")
  companion object {
    @JvmStatic
    fun getRootCAs() = entries.filter { it.isRootCA }
    fun getKompCAs() = entries.filter { !it.isRootCA }
  }
}
