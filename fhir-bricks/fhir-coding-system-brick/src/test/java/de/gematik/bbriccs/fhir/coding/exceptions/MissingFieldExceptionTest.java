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

package de.gematik.bbriccs.fhir.coding.exceptions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.utils.TestCodeSystem;
import de.gematik.bbriccs.fhir.coding.utils.TestProfileValueSet;
import de.gematik.bbriccs.fhir.coding.utils.TestValueSystem;
import lombok.val;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class MissingFieldExceptionTest {

  @Test
  void shouldCreateWithSingleSystem() {
    val system = new TestValueSystem();
    val mfe = new MissingFieldException(Task.class, system);

    val message = mfe.getMessage();
    assertTrue(message.contains("Missing Field"));
    assertTrue(message.contains(system.getCanonicalUrl()));
    assertTrue(message.contains(Task.class.getSimpleName()));
  }

  @Test
  void shouldCreateWithSingleValueSetElement() {
    val mfe = new MissingFieldException(Task.class, TestProfileValueSet.AA);

    val message = mfe.getMessage();
    assertTrue(message.contains("Missing Field"));
    assertTrue(message.contains(TestProfileValueSet.AA.name()));
    assertFalse(message.contains(TestProfileValueSet.AB.name()));
    assertTrue(message.contains(TestCodeSystem.TYPE_A.getCanonicalUrl()));
    assertTrue(message.contains(Task.class.getSimpleName()));
  }

  @Test
  void shouldCreateWithMultipleValueSetElements() {
    val mfe = new MissingFieldException(Task.class, TestProfileValueSet.AA, TestProfileValueSet.AB);

    val message = mfe.getMessage();
    assertTrue(message.contains("Missing Field"));
    assertTrue(message.contains(TestProfileValueSet.AA.name()));
    assertTrue(message.contains(TestProfileValueSet.AB.name()));
    assertTrue(message.contains(TestCodeSystem.TYPE_A.getCanonicalUrl()));
    assertTrue(message.contains(Task.class.getSimpleName()));
  }

  @Test
  void shouldCreateWithSingleResourceType() {
    val mfe = new MissingFieldException(Task.class, ResourceType.Patient);

    val message = mfe.getMessage();
    assertTrue(message.contains("Patient"));
    assertTrue(message.contains(Task.class.getSimpleName()));
  }

  @Test
  void shouldCreateWithMultipleResourceTypes() {
    val mfe = new MissingFieldException(Task.class, ResourceType.Patient, ResourceType.Coverage);

    val message = mfe.getMessage();
    assertTrue(message.contains("Patient"));
    assertTrue(message.contains("Coverage"));
    assertTrue(message.contains(Task.class.getSimpleName()));
  }
}
