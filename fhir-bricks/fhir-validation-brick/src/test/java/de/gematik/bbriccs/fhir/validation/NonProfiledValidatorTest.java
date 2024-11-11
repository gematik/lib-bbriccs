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

package de.gematik.bbriccs.fhir.validation;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.validation.utils.FhirValidatingTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class NonProfiledValidatorTest extends FhirValidatingTest {

  @Override
  protected void initialize() {
    this.fhirValidator = new NonProfiledValidator();
  }

  @ParameterizedTest(name = "[{index}] Should not throw on invalid File ''{0}''")
  @MethodSource
  void shouldValidateInvalidResources(String file, String content) {
    log.trace(format("Validate invalid file: {0}", file));
    assertFalse(this.fhirValidator.isValid(content));
  }

  static Stream<Arguments> shouldValidateInvalidResources() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/invalid", true);
    return files.stream().map(f -> Arguments.arguments(f.getName(), ResourceLoader.readString(f)));
  }

  @ParameterizedTest(name = "[{index}] Should validate valid example {0}")
  @MethodSource
  void shouldValidateValidResources(String file, String content) {
    log.debug(format("Validate valid file: {0}", file));
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
