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

package de.gematik.bbriccs.vsdm.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.VsdmUtils;
import java.time.Instant;
import java.util.Base64;
import lombok.val;
import org.junit.jupiter.api.Test;

class VsdmPatientTest {
  @Test
  void shouldGenerateHashSuccessful() {
    VsdmPatient patient =
        new VsdmPatient(
            VsdmKvnr.from("A123456789"),
            false,
            Instant.parse("2018-01-11T07:00:00Z"),
            "Beispielstrasse");
    assertEquals(
        "10be65f365",
        VsdmUtils.bytesToHex(patient.generateField1()).toLowerCase().replace(" ", ""));

    patient =
        new VsdmPatient(
            VsdmKvnr.from("A234567891"), false, Instant.parse("2018-01-11T07:00:00Z"), "");
    assertEquals(
        "40851ebe59",
        VsdmUtils.bytesToHex(patient.generateField1()).toLowerCase().replace(" ", ""));

    patient =
        new VsdmPatient(
            VsdmKvnr.from("A345678912"), true, Instant.parse("2018-01-11T07:00:00Z"), "");
    assertEquals(
        "c0851ebe59",
        VsdmUtils.bytesToHex(patient.generateField1()).toLowerCase().replace(" ", ""));
  }

  @Test
  void shouldNotThrowExceptionForValidData() {
    val timestamp = Instant.parse("2025-01-01T00:00:00Z");
    val patient = new VsdmPatient(VsdmKvnr.from("X123456789"), true, timestamp, "EmptyStreet");
    assertDoesNotThrow(patient::generateField1);
    assertEquals("rVtWLyc=", Base64.getEncoder().encodeToString(patient.generateField1()));
  }
}
