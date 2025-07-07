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

package de.gematik.bbriccs.fhir.de.value;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidSystemException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import lombok.val;
import org.junit.jupiter.api.Test;

class TelematikIDTest {

  @Test
  void shouldGenerateRandomTelematikId() {
    val tid = TelematikID.random();
    assertEquals(DeBasisProfilNamingSystem.TELEMATIK_ID_SID, tid.getSystem());
    assertNotNull(tid.getValue());
  }

  @Test
  void shouldExtractTelematikIdFromIdentifier() {
    val tidValue = "3-SMC-B-Testkarte-883110000116873";
    val identifier = DeBasisProfilNamingSystem.TELEMATIK_ID_SID.asIdentifier(tidValue);

    val tid = assertDoesNotThrow(() -> TelematikID.from(identifier));
    assertTrue(DeBasisProfilNamingSystem.TELEMATIK_ID_SID.matches(tid.getSystem()));
    assertEquals(tidValue, tid.getValue());
  }

  @Test
  void shouldThrowOnExtractingTelematikIdFromWrongIdentifier() {
    val tidValue = "3-SMC-B-Testkarte-883110000116873";
    val identifier = DeBasisProfilNamingSystem.KVID_GKV_SID.asIdentifier(tidValue);

    assertThrows(InvalidSystemException.class, () -> TelematikID.from(identifier));
  }
}
