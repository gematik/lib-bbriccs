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

import de.gematik.bbriccs.fhir.validation.utils.FhirValidatingTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class NonProfiledValidatorTest extends FhirValidatingTest {

  private static final ValidatorFhir MY_VALIDATOR = new NonProfiledValidator();

  @Override
  protected void initialize() {
    this.fhirValidator = MY_VALIDATOR;
  }

  @ParameterizedTest(name = "[{index}] Should not throw on invalid File ''{0}''")
  @MethodSource
  void shouldValidateInvalidResourceContents(String file, String content) {
    log.trace("Validate invalid file: {}", file);
    assertFalse(this.fhirValidator.isValid(content));
  }

  static Stream<Arguments> shouldValidateInvalidResourceContents() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/invalid", true);
    return files.stream().map(f -> Arguments.arguments(f.getName(), ResourceLoader.readString(f)));
  }

  @Test
  void shouldValidateInvalidResources() {
    val resource = new Bundle();
    assertFalse(this.fhirValidator.isValid(resource));

    val vr = this.fhirValidator.validate(resource);
    assertFalse(vr.getMessages().isEmpty());
  }

  @ParameterizedTest(name = "[{index}] Should validate valid example {0}")
  @MethodSource
  void shouldValidateValidResources(String file, String content) {
    log.debug("Validate valid file: {}", file);
    assertTrue(this.fhirValidator.isValid(content));
  }

  static Stream<Arguments> shouldValidateValidResources() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid", true);
    return files.stream()
        .map(f -> Arguments.arguments(f.getAbsolutePath(), ResourceLoader.readString(f)));
  }

  @Test
  void shouldProvideFhirContext() {
    val validator = new NonProfiledValidator();
    assertNotNull(validator.getContext());
  }
}
