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

package de.gematik.bbriccs.fhir.coding;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.utils.TestValueSystem;
import lombok.val;
import org.junit.jupiter.api.Test;

class SemanticValueTest {

  private static final String TEST_VALUE_SYSTEM = new TestValueSystem().getCanonicalUrl();

  @Test
  void shouldGetSystemAndValue() {
    val value = new TestValue("123");
    assertEquals("123", value.getValue());
    assertEquals(TEST_VALUE_SYSTEM, value.getSystemUrl());
    assertTrue(value.toString().contains(TEST_VALUE_SYSTEM));
    assertTrue(value.toString().contains("123"));
  }

  @Test
  void shouldEncodeAsIdentifier() {
    val value = new TestValue("123");
    val identifier = value.asIdentifier();

    assertNotNull(identifier);
    assertEquals("123", identifier.getValue());
    assertEquals(TEST_VALUE_SYSTEM, identifier.getSystem());
  }

  @Test
  void shouldEncodeAsReference() {
    val value = new TestValue("123");
    val reference = value.asReference();
    assertNotNull(reference);

    val identifier = reference.getIdentifier();
    assertNotNull(identifier);
    assertEquals("123", identifier.getValue());
    assertEquals(TEST_VALUE_SYSTEM, identifier.getSystem());
  }

  @Test
  void shouldEncodeAsCoding() {
    val value = new TestValue("123");
    val coding = value.asCoding();

    assertNotNull(coding);
    assertEquals("123", coding.getCode());
    assertEquals(TEST_VALUE_SYSTEM, value.getSystemUrl());
  }

  @Test
  void shouldEncodeAsCodeableConcept() {
    val value = new TestValue("123");
    val codable = value.asCodeableConcept();
    assertNotNull(codable);

    val coding = codable.getCodingFirstRep();
    assertNotNull(coding);

    assertEquals("123", coding.getCode());
    assertEquals(TEST_VALUE_SYSTEM, value.getSystemUrl());
  }
}
