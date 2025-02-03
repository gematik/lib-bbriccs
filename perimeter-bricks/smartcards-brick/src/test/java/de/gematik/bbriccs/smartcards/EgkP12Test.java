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

package de.gematik.bbriccs.smartcards;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.InvalidCertificateException;
import de.gematik.bbriccs.smartcards.exceptions.MissingCardAttribute;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EgkP12Test {

  private static SmartcardArchive archive;

  @BeforeAll
  static void setupArchive() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    archive = SmartcardArchive.from(archiveFile);
  }

  @Test
  void shouldGetPrivateKeysAsBase64() {
    archive
        .getICCSNsFor(SmartcardType.EGK)
        .forEach(egk -> assertDoesNotThrow(() -> archive.getEgkByICCSN(egk).getPrivateKeyBase64()));
  }

  @Test
  void shouldGetDefaultKey() {
    List.of(CryptoSystem.ECC_256, CryptoSystem.RSA_2048)
        .forEach(
            crpyto ->
                archive.getICCSNsFor(SmartcardType.EGK).stream()
                    .map(iccsn -> archive.getEgkByICCSN(iccsn))
                    .forEach(egk -> assertTrue(egk.getAutCertificate(crpyto).isPresent())));
  }

  @Test
  void shouldThrowOnMissingKvnrInCertificates() {
    val egk = archive.getEgk(0);
    try (val mockLdap = mockStatic(LdapReader.class)) {
      mockLdap
          .when(
              () ->
                  LdapReader.getOwnerData(
                      egk.getAutCertificate().getX509Certificate().getSubjectX500Principal()))
          .thenReturn(
              SmartcardOwnerData.builder()
                  .commonName("Max Mustermann")
                  .organizationUnit("123")
                  .build());

      val certificates = List.of(egk.getAutCertificate());
      val config = new SmartcardConfigDto();
      config.setType(SmartcardType.EGK);
      assertThrows(InvalidCertificateException.class, () -> new EgkP12(config, certificates));
    }
  }

  @Test
  void shouldGetInsuranceStartDate() {
    archive
        .getICCSNsFor(SmartcardType.EGK)
        .forEach(
            egk -> assertDoesNotThrow(() -> archive.getEgkByICCSN(egk).getInsuranceStartDate()));
  }

  @Test
  void shouldThrowOnMissingInsuranceStartDate() {
    val egk = mock(EgkP12.class);
    when(egk.getAutCertificate(CryptoSystem.ECC_256)).thenReturn(Optional.empty());
    when(egk.getAutCertificate(CryptoSystem.RSA_2048)).thenReturn(Optional.empty());
    doCallRealMethod().when(egk).getInsuranceStartDate();
    assertThrows(MissingCardAttribute.class, egk::getInsuranceStartDate);
  }
}
