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

package de.gematik.bbriccs.profiles.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidSystemException;
import de.gematik.bbriccs.profiles.utils.TestProfileValueSet;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class InvalidSystemExceptionTest {

  @Test
  void shouldContainNotAllowed() {
    val ise = new InvalidSystemException(Task.class, TestProfileValueSet.AA.getCodeSystem());
    assertTrue(ise.getMessage().contains("not allowed"));
    assertTrue(ise.getMessage().contains(Task.class.getSimpleName()));
  }
}