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

package de.gematik.bbriccs.fhir.validation;

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DummyValidatorTest {

  static Stream<Arguments> invalidResources() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/invalid", true);
    return files.stream().map(f -> Arguments.arguments(f.getName(), ResourceLoader.readString(f)));
  }

  @ParameterizedTest(name = "[{index}] Validate invalid File ''{0}'' with DummyValidator")
  @MethodSource("invalidResources")
  void shouldValidateInvalidResourceContents(String file, String content) {
    val validator = new DummyValidator(FhirContext.forR4());
    assertTrue(validator.isValid(content));

    val vr = validator.validate(content);
    assertEquals(1, vr.getMessages().size());
    assertEquals(ResultSeverityEnum.INFORMATION, vr.getMessages().get(0).getSeverity());

    assertNotNull(validator.getContext());
  }

  @Test
  void shouldValidateInvalidResources() {
    val validator = new DummyValidator(FhirContext.forR4());
    val resource = new Bundle();
    assertTrue(validator.isValid(resource));

    val vr = validator.validate(resource);
    assertEquals(1, vr.getMessages().size());
    assertEquals(ResultSeverityEnum.INFORMATION, vr.getMessages().get(0).getSeverity());

    assertNotNull(validator.getContext());
  }
}
