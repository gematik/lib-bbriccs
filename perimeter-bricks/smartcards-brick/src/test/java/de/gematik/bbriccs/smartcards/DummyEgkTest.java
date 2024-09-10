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

import de.gematik.bbriccs.crypto.certificate.Oid;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class DummyEgkTest {

  @Test
  void shouldGenerateDummyEgkFromConfig() {
    val sca = SmartcardArchive.fromResources();
    val config = sca.getConfigsFor(SmartcardType.EGK).get(0);
    val egk = DummyEgk.fromConfig(config);
    assertNotNull(egk);
    assertEquals(config.getIccsn(), egk.getIccsn());
    assertEquals(config.getIdentifier(), egk.getKvnr());
    assertEquals(SmartcardType.EGK, egk.getType());
    assertEquals(List.of(Oid.OID_EGK_AUT, Oid.OID_EGK_AUT_ALT), egk.getAutOids());
    assertNotNull(egk.getOwnerData());
    assertTrue(egk.getOwnerData().getCommonName().contains(config.getOwnerName()));

    assertNull(egk.getAutCertificate());
    assertNull(egk.getAuthPrivateKey());
    assertNull(egk.getAuthPublicKey());
    assertTrue(egk.getPrivateKeyBase64().isEmpty());

    val extension = assertDoesNotThrow(() -> egk.getExtensionAs(DummyExtension.class));
    assertNotNull(extension);
  }

  private static class DummyExtension implements SmartcardExtension {}
}
