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

package de.gematik.bbriccs.fhir.coding.version;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.utils.TestBasisClassVersion;
import de.gematik.bbriccs.fhir.coding.utils.TestBasisVersion;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProfileVersionTest {

  @Test
  void shouldCompareVersions01() {
    val v1 = TestBasisVersion.V1_3_2;
    val v2 = TestBasisVersion.V0_9_13;

    assertEquals(1, v1.compareTo(v2));
    assertEquals(-1, v2.compareTo(v1));
    assertEquals(0, v1.compareTo(TestBasisVersion.V1_3_2));
  }

  @Test
  void shouldCompareVersions02() {
    val v1 = new GenericProfileVersion("1.0.0");
    val v2 = new GenericProfileVersion("1.0.0");

    assertEquals(0, v1.compareTo(v2));
  }

  @Test
  void shouldCompareVersions03() {
    val v1 = new GenericProfileVersion("1.0.0");
    val v2 = new TestBasisClassVersion("1.0.0");

    assertEquals(-1, v1.compareTo(v2));
  }

  @Test
  void shouldTakeMissingPatchIntoAccount01() {
    val v1 = new GenericProfileVersion("1.0.0");

    // 1.0.0 and 1.0 are technically considered the same!
    assertEquals(0, v1.compareTo("1.0"));
  }

  @Test
  void shouldTakeMissingPatchIntoAccount02() {
    val v1 = new GenericProfileVersion("1.0");

    // 1.0.0 and 1.0 are technically considered the same!
    assertEquals(0, v1.compareTo("1.0.0"));
  }
}
