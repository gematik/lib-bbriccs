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

package de.gematik.bbriccs.konnektor.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.Arrays;
import javax.security.auth.x500.X500Principal;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class BNetzAVLCaTest {

  @Test
  void certificateShouldNotNull() {
    Arrays.stream(BNetzAVLCa.values())
        .forEach(
            bNetzAVLCa -> {
              val certificate = bNetzAVLCa.getCertificate();
              assertNotNull(certificate);
            });
  }

  @Test
  void shouldThrowExceptionWhenSubjectCANotFound() {
    assertThrows(IllegalArgumentException.class, () -> BNetzAVLCa.getCA("Invalid Subject CA"));
  }

  @Test
  void shouldReturnCorrectValueWhenSubjectCAExists() {
    val bNetzAVLCa = BNetzAVLCa.getCA("GEM.HBA-qCA6 TEST-ONLY");
    assertEquals(BNetzAVLCa.GEM_HBA_QCA6_TEST_ONLY.getCertificate(), bNetzAVLCa);
  }

  @Test
  void shouldThrowExceptionWhenEECertificatesNotExists() {
    val eeCert = BNetzAVLCa.GEM_HBA_QCA24_TEST_ONLY.getCertificate();
    assertThrows(IllegalArgumentException.class, () -> BNetzAVLCa.getCA(eeCert));
  }

  @Test
  void shouldThrowExceptionWhenEECertificatesDoesNotHaveIssuerName() {
    val eeCert = spy(BNetzAVLCa.GEM_HBA_QCA24_TEST_ONLY.getCertificate());
    val principal = mock(X500Principal.class);
    when(principal.getName()).thenReturn("O=gematik GmbH NOT-VALID,C=DE");
    when(eeCert.getIssuerX500Principal()).thenReturn(principal);

    val e = assertThrows(IllegalArgumentException.class, () -> BNetzAVLCa.getCA(eeCert));
    System.out.println(e.getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldReturnCertificateWhenEECertificatesIssuerExists(CryptoSystem algorithm) {
    val hba = SmartcardArchive.fromResources().getHba(0);
    val eeCert = hba.getQesCertificate(algorithm);
    assertNotNull(BNetzAVLCa.getCA(eeCert.getX509Certificate()));
  }
}