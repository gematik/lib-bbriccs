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

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class SmcBP12Test {

  private static final SmartcardArchive archive = SmartcardArchive.fromResources();

  @Test
  void getOSigCertificate() {
    val smcb = archive.getSmcB(0);
    assertNotNull(smcb.getOSigCertificate(CryptoSystem.RSA_2048));
  }

  @Test
  void shouldThrowOnMissingOSigCertificate() {
    val smcb = archive.getSmcbByICCSN("80276001011699901102");
    assertThrows(
        SmartCardKeyNotFoundException.class,
        () -> smcb.getOSigCertificate(CryptoSystem.RSA_PSS_2048));
  }

  @Test
  void getEncCertificate() {
    val smcb = archive.getSmcB(0);
    assertNotNull(smcb.getEncCertificate(CryptoSystem.RSA_2048));
  }

  @Test
  void getAutOids() {
    val smcb = archive.getSmcB(0);
    assertEquals(List.of(Oid.OID_SMC_B_AUT), smcb.getAutOids());
  }
}
