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

package de.gematik.bbriccs.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import de.gematik.bbriccs.rest.HttpBClient
import de.gematik.bbriccs.rest.UnirestHttpClient
import de.gematik.bbriccs.utils.dto.CertificateAuthorityDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.cert.X509Certificate
import java.util.stream.Stream

class CertificateAuthoritySupplierTest {

  private lateinit var subCAs: Set<CertificateAuthorityDto>
  private lateinit var rootCAs: RootCertificateAuthorityList
  private lateinit var wireMockServer: WireMockServer
  private lateinit var httpBClient: HttpBClient

  @BeforeEach
  fun setup() {
    // Start WireMock server
    wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    wireMockServer.start()

    httpBClient = UnirestHttpClient.forUrl("http://localhost:${wireMockServer.port()}").withoutTlsVerification()

    wireMockServer.stubFor(
      get(urlEqualTo("/ECC/${CaType.ROOT_CA}/roots.json"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Files.readString(Paths.get("src/test/resources/roots.json"))),
        ),
    )

    wireMockServer.stubFor(
      get(urlEqualTo("/ECC/${CaType.SUB_CA}/"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Certs.entries.joinToString(separator = " ") { it.href }),
        ),
    )

    Certs.entries.forEach { cert ->
      wireMockServer.stubFor(
        get(urlEqualTo("/ECC/${CaType.SUB_CA}/${cert.caName}.der"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(cert.cert.encoded),
          ),
      )
    }

    rootCAs = CertificateAuthoritySupplier.builder().useHttpClient(httpBClient).getRootCAsFromBackend()
    subCAs = CertificateAuthoritySupplier.builder().useHttpClient(httpBClient).getSubCAsFromBackend()
  }

  @AfterEach
  fun tearDown() {
    wireMockServer.stop()
  }

  // Add the testDataProvider method
  companion object {
    @JvmStatic
    fun testDataProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of("GEM.RCA3 TEST-ONLY", "GEM.RCA8 TEST-ONLY", listOf("GEM.RCA3 TEST-ONLY", "GEM.RCA4 TEST-ONLY", "GEM.RCA5 TEST-ONLY", "GEM.RCA6 TEST-ONLY", "GEM.RCA7 TEST-ONLY")),
        Arguments.of("GEM.RCA4 TEST-ONLY", "GEM.RCA7 TEST-ONLY", listOf("GEM.RCA4 TEST-ONLY", "GEM.RCA5 TEST-ONLY", "GEM.RCA6 TEST-ONLY")),
        Arguments.of("GEM.RCA5 TEST-ONLY", "GEM.RCA6 TEST-ONLY", listOf("GEM.RCA5 TEST-ONLY")),
        Arguments.of("GEM.RCA7 TEST-ONLY", "GEM.RCA2 TEST-ONLY", listOf("GEM.RCA5 TEST-ONLY")),
      )
    }
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

  @ParameterizedTest
  @MethodSource("testDataProvider")
  fun `should return the chain of all Cross-Root CAs between`(startSubject: String, targetSubject: String, crossCASubjects: List<String>) {
    val start = rootCAs.getRootCABy(startSubject) ?: throw AssertionError("$startSubject not found")
    val target = rootCAs.getRootCABy(targetSubject) ?: throw AssertionError("$targetSubject not found")

    val nextCrossCa: (String) -> X509Certificate? =
      if (start < target) {
        {
            subject ->
          rootCAs.getRootCABy(subject)?.nextCrossCA
        }
      } else {
        {
            subject ->
          rootCAs.getRootCABy(subject)?.prevCrossCA
        }
      }
    rootCAs.getChainOfCrossRootCAsBetween(start, target).let { chain ->
      crossCASubjects.forEach {
        val nextCa = nextCrossCa(it)
        assertNotNull(nextCa, "$it not found in chain")
        assertTrue(chain.contains(nextCa), "$it not found in chain")
      }
    }
  }

  @Test
  fun `should calculate the correct chain of CrossCAs from a KOMP CA up to a RootCA`() {
    val rootCaFirst = rootCAs.getRootCABy("GEM.RCA5 TEST-ONLY") ?: throw AssertionError("RootCA5 not found")

    assertDoesNotThrow { rootCAs.getChainOfCrossRootCAByCompCAs(rootCaFirst, listOf(Certs.KOMP_CA54.cert)) }
    rootCAs.getChainOfCrossRootCAByCompCAs(rootCaFirst, listOf(Certs.KOMP_CA54.cert)).let { list ->
      assertEquals(1, list.size)
      list[0].let {
        assertEquals("GEM.RCA6 TEST-ONLY", it.getSubjectCN())
        assertEquals("GEM.RCA5 TEST-ONLY", it.getIssuerCN()) // Start Point to min (GEM.RCA2)
      }
    }
  }

  @Test
  fun `should calculate the correct chain of CrossCAs for a given RootCA`() {
    val rootCaFirst = rootCAs.getRootCABy("GEM.RCA5 TEST-ONLY") ?: throw AssertionError("RootCA5 not found")

    assertDoesNotThrow { rootCAs.getChainOfCrossRootCAByCompCAs(rootCaFirst) }
    rootCAs.getChainOfCrossRootCAByCompCAs(rootCaFirst).let { list ->
      assertEquals(9, list.size)
      list[0].let {
        assertEquals("GEM.RCA6 TEST-ONLY", it.getSubjectCN())
        assertEquals("GEM.RCA5 TEST-ONLY", it.getIssuerCN())
      }
      list[1].let {
        assertEquals("GEM.RCA7 TEST-ONLY", it.getSubjectCN())
        assertEquals("GEM.RCA6 TEST-ONLY", it.getIssuerCN())
      }
      list[5].let {
        assertEquals("GEM.RCA11 TEST-ONLY", it.getSubjectCN())
        assertEquals("GEM.RCA10 TEST-ONLY", it.getIssuerCN())
      }
      list[8].let {
        assertEquals("GEM.RCA2 TEST-ONLY", it.getSubjectCN())
        assertEquals("GEM.RCA3 TEST-ONLY", it.getIssuerCN())
      }
    }
  }

  @Test
  fun `Builder should not throw exceptions`() {
    assertDoesNotThrow { CertificateAuthoritySupplier.Builder().useTI() }
    assertDoesNotThrow { CertificateAuthoritySupplier.Builder().useInternet() }
    assertDoesNotThrow { CertificateAuthoritySupplier.Builder().withEnvironmentAnchor(TiTrustedEnvironmentAnchor.RU) }
  }

  @ParameterizedTest
  @EnumSource(TiTrustedEnvironmentAnchor::class)
  @Disabled
  fun `Integration Test`(anchor: TrustedEnvironmentAnchor) {
    val rootCAs = CertificateAuthoritySupplier.Builder().withEnvironmentAnchor(anchor).getRootCAsFromBackend()
    rootCAs.forEach { ca ->
      assertDoesNotThrow { ca.getSubjectCN() }
      assertDoesNotThrow { ca.getIssuerCN() }
      assertDoesNotThrow { ca.getCaNumber() }
      assertDoesNotThrow { ca.isCrossCa() }
      assertNotNull(ca.cert)
    }

    val subCA = CertificateAuthoritySupplier.Builder().withEnvironmentAnchor(anchor).getSubCAsFromBackend()
    subCA.forEach { ca ->
      assertDoesNotThrow { ca.getSubjectCN() }
      assertDoesNotThrow { ca.getIssuerCN() }
      assertNotNull(ca.cert)
    }
  }

  @Test
  fun `nextRootCA and prevRootCA should return the next or the previous RootCA`() {
    rootCAs.getRootCABy("GEM.RCA1 TEST-ONLY")?.let {
      assertNull(rootCAs.prevRootCA(it))
      assertNotNull(rootCAs.nextRootCA(it))
    }

    rootCAs.getRootCABy("GEM.RCA11 TEST-ONLY")?.let {
      assertNull(rootCAs.nextRootCA(it))
      assertNotNull(rootCAs.prevRootCA(it))
    }
  }
}

enum class Certs(val caName: String) {
  KOMP_CA51("GEM.KOMP-CA51_TEST-ONLY"),
  KOMP_CA54("GEM.KOMP-CA54_TEST-ONLY"),
  KOMP_CA24("GEM.KOMP-CA24_TEST-ONLY"),
  KOMP_CA56("GEM.KOMP-CA56_TEST-ONLY"),
  ;

  val cert = javaClass.classLoader.getResourceAsStream("${this.caName}.der")?.toCertificate()
    ?: throw FileNotFoundException("$caName not found in resources folder")

  val href = "<a href=\"$caName.der\">"
}
