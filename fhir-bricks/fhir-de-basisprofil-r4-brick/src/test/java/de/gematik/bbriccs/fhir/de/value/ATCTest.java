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

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

class ATCTest {

  @Test
  void shouldBuildFromCoding() {
    val atc = ATC.from(new Coding("http://fhir.de/CodeSystem/bfarm/atc", "M01AE01", "Ibuprofen"));

    assertEquals(DeBasisProfilCodeSystem.ATC, atc.getSystem());
    assertTrue(atc.getDisplay().isPresent());
    assertTrue(atc.getVersion().isEmpty());
  }

  @Test
  void shouldBuildFromCodeWithDisplayAndVersion() {
    val atc = ATC.from("M01AE01", "Ibuprofen", "2022");
    assertTrue(atc.getSystem().matches(DeBasisProfilCodeSystem.ATC));
    assertEquals("Ibuprofen", atc.getDisplay().orElseThrow());
    assertEquals("2022", atc.getVersion().orElseThrow());
  }

  @Test
  void shouldBuildFromCodeSimple() {
    val atc = ATC.from("M01AE01");
    assertTrue(atc.getSystem().matches(DeBasisProfilCodeSystem.ATC));
    assertTrue(atc.getVersion().isEmpty());
    assertTrue(atc.getDisplay().isEmpty());
  }
}
