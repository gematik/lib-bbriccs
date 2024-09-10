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

package de.gematik.bbriccs.fhir.builder;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;

class OperationOutcomeBuilderTest extends FhirTest {

  @Override
  protected void initialize() {
    this.printEncoded = false;
    this.prettyPrint = false;
  }

  @Test
  void shouldBuildOperationOutcomeWithFixedValues() {
    val oo =
        OperationOutcomeBuilder.create()
            .withIssue()
            .diagnostics("additional diagnostics about the error")
            .severity(OperationOutcome.IssueSeverity.ERROR)
            .withDetailsText("error details")
            .narrativeText("TEST")
            .build();
    val result = this.encodeAndValidate(oo);
    assertTrue(result.isSuccessful());
  }
}
