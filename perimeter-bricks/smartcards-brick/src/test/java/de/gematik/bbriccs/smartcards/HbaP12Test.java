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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.IOException;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HbaP12Test {

  private static SmartcardArchive archive;

  @BeforeAll
  static void setupArchive() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    archive = SmartcardArchive.from(archiveFile);
  }

  @Test
  void shouldReadQesFromSupplier() {
    val first =
        archive.getICCSNsFor(SmartcardType.HBA).stream()
            .map(iccsn -> archive.getHbaByICCSN(iccsn))
            .findFirst()
            .orElseThrow();
    val supplier = first.getQesCertificate(CryptoSystem.RSA_2048).getCertificateStream();
    try (val stream = supplier.get()) {
      assertNotNull(stream);
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void shouldEqualOnSame() {
    val first = archive.getHba(0);
    val second = archive.getHba(0);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void shouldNotEqualOnNull() {
    val iccsn = "80276001011699901501";
    val first = archive.getHbaByICCSN(iccsn);
    assertNotEquals(null, first); // NOSONAR
  }

  @Test
  void shouldNotEqualOnDifferentCards() {
    val first = archive.getHba(0);
    val second = archive.getEgk(0);
    assertNotEquals(first, second);
  }

  @Test
  void getTelematikId() {
    val hba = archive.getHba(0);
    assertNotNull(hba.getTelematikId());
  }

  @Test
  void getEncCertificate() {
    val hba = archive.getHba(0);
    assertDoesNotThrow(() -> hba.getEncCertificate(CryptoSystem.RSA_2048));
    assertNotNull(hba.getEncCertificate(CryptoSystem.RSA_2048));
  }

  @Test
  void shouldThrowSmartCardKeyNotFoundException() {
    val hba = archive.getHba(0);
    assertThrows(
        SmartCardKeyNotFoundException.class,
        () -> hba.getEncCertificate(CryptoSystem.RSA_PSS_2048));
  }
}
