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

package de.gematik.bbriccs.fhir.coding.version;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.utils.TestBasisClassVersion;
import de.gematik.bbriccs.fhir.coding.utils.TestBasisVersion;
import de.gematik.bbriccs.fhir.coding.utils.TestBasisVersion.TestBasisVersion2;
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

    assertTrue(v1.isEqual(v2));
    assertTrue(v1.isSmallerThanOrEqualTo(v2));
    assertFalse(v1.isSmallerThan(v2));

    assertTrue(v1.isBiggerThanOrEqualTo(v2));
    assertFalse(v1.isBiggerThan(v2));
  }

  @Test
  void shouldCompareVersions04() {
    // v1.3.2 is greater than v1.3 -> v1.3.0
    assertTrue(TestBasisVersion.V1_3_2.isBiggerThan(TestBasisVersion.V0_9_13));
    assertTrue(TestBasisVersion.V1_3_2.isBiggerThan(TestBasisVersion2.V1_3));
    assertTrue(TestBasisVersion.V1_3_2.isBiggerThanOrEqualTo(TestBasisVersion2.V1_3));
    assertTrue(TestBasisVersion.V1_3_2.isBiggerThanOrEqualTo(TestBasisVersion.V1_3_2));
    assertFalse(TestBasisVersion2.V1_3.isBiggerThan(TestBasisVersion.V1_3_2));

    // v0.9 -> v0.9.0 is smaller than v0.9.13
    assertTrue(TestBasisVersion2.V0_9.isSmallerThan(TestBasisVersion.V0_9_13));
    assertFalse(TestBasisVersion.V0_9_13.isSmallerThan(TestBasisVersion2.V0_9));
    assertFalse(TestBasisVersion.V0_9_13.isSmallerThanOrEqualTo(TestBasisVersion2.V0_9));
    assertTrue(TestBasisVersion.V0_9_13.isSmallerThanOrEqualTo(TestBasisVersion.V0_9_13));
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
