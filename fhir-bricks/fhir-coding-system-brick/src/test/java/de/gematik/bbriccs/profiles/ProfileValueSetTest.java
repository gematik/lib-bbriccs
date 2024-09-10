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

package de.gematik.bbriccs.profiles;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.ProfileValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.bbriccs.profiles.utils.TestCodeSystem;
import de.gematik.bbriccs.profiles.utils.TestProfileValueSet;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProfileValueSetTest {

  @Test
  void shouldCreateAsCoding() {
    val coding = TestProfileValueSet.AA.asCoding();
    assertNull(coding.getDisplay()); // without display by default
    assertEquals("AA", coding.getCode());
    assertEquals(TestCodeSystem.TYPE_A.getCanonicalUrl(), coding.getSystem());
  }

  @Test
  void shouldCreateAsCodingWithCustomSystem() {
    val coding = TestProfileValueSet.AA.asCoding(TestCodeSystem.TYPE_B);
    assertNull(coding.getDisplay()); // without display by default
    assertEquals("AA", coding.getCode());
    assertEquals(TestCodeSystem.TYPE_B.getCanonicalUrl(), coding.getSystem());
  }

  @Test
  void shouldCreateAsCodeableWithoutDisplay() {
    val cc = TestProfileValueSet.AA.asCodeableConcept();
    assertFalse(cc.getCoding().isEmpty());
    assertNull(cc.getCodingFirstRep().getDisplay());
    assertEquals("AA", cc.getCodingFirstRep().getCode());
    assertEquals(TestCodeSystem.TYPE_A.getCanonicalUrl(), cc.getCodingFirstRep().getSystem());
  }

  @Test
  void shouldCreateAsCodeableWithCustomSystem() {
    val cc = TestProfileValueSet.AA.asCodeableConcept(TestCodeSystem.TYPE_B);
    assertFalse(cc.getCoding().isEmpty());
    assertNull(cc.getCodingFirstRep().getDisplay());
    assertEquals("AA", cc.getCodingFirstRep().getCode());
    assertEquals(TestCodeSystem.TYPE_B.getCanonicalUrl(), cc.getCodingFirstRep().getSystem());
  }

  @Test
  void shouldCreateAsCodeableWithDisplay() {
    val cc = TestProfileValueSet.AB.asCodeableConcept(true);
    assertFalse(cc.getCoding().isEmpty());
    assertEquals("second value", cc.getCodingFirstRep().getDisplay());
    assertEquals("AB", cc.getCodingFirstRep().getCode());
    assertEquals(TestCodeSystem.TYPE_A.getCanonicalUrl(), cc.getCodingFirstRep().getSystem());
  }

  @Test
  void shouldCreateFromCode() {
    val vs = assertDoesNotThrow(() -> ProfileValueSet.fromCode(TestProfileValueSet.class, "AA"));
    assertEquals(TestProfileValueSet.AA, vs);
  }

  @Test
  void shouldThrowOnInvalidCode() {
    assertThrows(
        InvalidValueSetException.class,
        () -> ProfileValueSet.fromCode(TestProfileValueSet.class, "AC"));
  }
}
