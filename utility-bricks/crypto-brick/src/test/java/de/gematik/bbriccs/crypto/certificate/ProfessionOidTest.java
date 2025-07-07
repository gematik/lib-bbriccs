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

package de.gematik.bbriccs.crypto.certificate;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.exceptions.UnsupportedOidException;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProfessionOidTest {

  @Test
  void shouldThrowOnUnknownProfessionOid() {
    assertThrows(UnsupportedOidException.class, () -> ProfessionOid.fromStringOrThrow("123"));
  }

  @Test
  void shouldFindMatchProfessionOid() {
    val oidValue = "1.2.276.0.76.4.50";
    val oid = assertDoesNotThrow(() -> ProfessionOid.fromStringOrThrow(oidValue));
    assertEquals(ProfessionOid.PRAXIS_ARZT, oid);

    assertEquals(oidValue, oid.getValue());
  }
}
