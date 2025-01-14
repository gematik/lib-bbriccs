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

package de.gematik.bbriccs.fhir.coding;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.utils.TestCodeSystem;
import lombok.val;
import org.junit.jupiter.api.Test;

class WithCodeSystemTest {

  @Test
  void shouldCreateAsCodeable() {
    val cc = TestCodeSystem.TYPE_A.asCodeableConcept("AA");
    assertFalse(cc.getCoding().isEmpty());
    assertEquals("AA", cc.getCodingFirstRep().getCode());
    assertEquals(TestCodeSystem.TYPE_A.getCanonicalUrl(), cc.getCodingFirstRep().getSystem());
  }
}
