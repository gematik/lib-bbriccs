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

package de.gematik.bbriccs.konnektor.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.model.x509.CertificateToken;
import java.time.ZonedDateTime;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class OcspTokenGeneratorTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardArchive.fromResources();
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldCreateGeneratorWithCertificateToken(CryptoSystem cryptoSystem) {
    val cert = sca.getHba(0).getQesCertificate(cryptoSystem).getX509Certificate();
    val certToken = new CertificateToken(cert);
    assertDoesNotThrow(() -> OcspTokenGenerator.with(certToken));
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldCreateGeneratorWithX509Certificate(CryptoSystem cryptoSystem) {
    val cert = sca.getHba(0).getQesCertificate(cryptoSystem).getX509Certificate();
    assertDoesNotThrow(() -> OcspTokenGenerator.with(cert));
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldCreateSelfSignedGoodToken(CryptoSystem cryptoSystem) {
    val cert = sca.getHba(0).getQesCertificate(cryptoSystem).getX509Certificate();
    val now = ZonedDateTime.now();

    val generator = OcspTokenGenerator.with(cert);
    val token = assertDoesNotThrow(() -> generator.asSelfSignedToken(now, now));

    assertNotNull(token);
    assertEquals(CertificateStatus.GOOD, token.getStatus());
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldCreateSelfSignedRevokedToken(CryptoSystem cryptoSystem) {
    val cert = sca.getHba(0).getQesCertificate(cryptoSystem).getX509Certificate();
    val now = ZonedDateTime.now();

    val generator = OcspTokenGenerator.with(cert);
    val token = assertDoesNotThrow(() -> generator.asSelfSignedRevokedToken(now, now));

    assertNotNull(token);
    assertEquals(CertificateStatus.REVOKED, token.getStatus());
  }

  @Test
  void shouldThrowWhenCertificateHasUnknownIssuer() {
    // CA-Zertifikate selbst werden nicht von einer bekannten BNetzAVL-CA ausgestellt
    val caCert = BNetzAVLCa.GEM_HBA_QCA24_TEST_ONLY.getCertificate();
    assertThrows(IllegalArgumentException.class, () -> OcspTokenGenerator.with(caCert));
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldReturnNonNullSigningCertTokenInGoodToken(CryptoSystem cryptoSystem) {
    val cert = sca.getHba(0).getQesCertificate(cryptoSystem).getX509Certificate();
    val now = ZonedDateTime.now();

    val token = OcspTokenGenerator.with(cert).asSelfSignedToken(now, now);

    assertNotNull(token.getRelatedCertificate());
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldReturnNonNullSigningCertTokenInRevokedToken(CryptoSystem cryptoSystem) {
    val cert = sca.getHba(0).getQesCertificate(cryptoSystem).getX509Certificate();
    val now = ZonedDateTime.now();

    val token = OcspTokenGenerator.with(cert).asSelfSignedRevokedToken(now, now);

    assertNotNull(token.getRelatedCertificate());
  }
}
