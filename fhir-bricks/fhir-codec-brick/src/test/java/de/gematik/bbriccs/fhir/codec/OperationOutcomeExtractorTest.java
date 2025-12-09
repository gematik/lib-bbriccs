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

package de.gematik.bbriccs.fhir.codec;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.val;
import org.junit.jupiter.api.Test;

class OperationOutcomeExtractorTest {

  @Test
  void shouldExtractErrorDetails() {
    val oo = createOperationOutcome();
    val wrapper = OperationOutcomeExtractor.from(oo);
    assertDoesNotThrow(wrapper::toString);
  }

  @Test
  void shouldExtractErrorDetailsWithoutDiagnostics() {
    val oo = createOperationOutcome();
    oo.getIssueFirstRep().setDiagnostics(null);
    val wrapper = OperationOutcomeExtractor.from(oo);
    assertDoesNotThrow(wrapper::toString);
  }

  @Test
  void shouldExtractDirectly() {
    val oo = createOperationOutcome();
    assertDoesNotThrow(() -> OperationOutcomeExtractor.extractFrom(oo));
  }
}
