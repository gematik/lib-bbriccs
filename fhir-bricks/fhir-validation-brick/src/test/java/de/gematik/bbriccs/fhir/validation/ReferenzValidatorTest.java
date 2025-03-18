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
 */

package de.gematik.bbriccs.fhir.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.validation.utils.FhirValidatingTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.commons.exceptions.ValidationModuleInitializationException;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReferenzValidatorTest extends FhirValidatingTest {

  private static final ValidatorFhir MY_VALIDATOR =
      ReferenzValidator.withValidationModule(SupportedValidationModule.ERP);

  @Override
  protected void initialize() {
    this.fhirValidator = MY_VALIDATOR;
  }

  @Test
  void shouldThrowOnInvalidConfiguration() {
    val svm = mock(SupportedValidationModule.class);
    assertThrows(
        ValidationModuleInitializationException.class,
        () -> ReferenzValidator.withValidationModule(svm));
  }

  @Test
  void shouldValidateInvalidResources() {
    val resource = new Bundle();
    assertFalse(this.fhirValidator.isValid(resource));

    val vr = this.fhirValidator.validate(resource);
    assertFalse(vr.getMessages().isEmpty());
  }

  static Stream<Arguments> validErpResources() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv", true);
    return files.stream()
        .map(f -> Arguments.arguments(f.getAbsolutePath(), ResourceLoader.readString(f)));
  }

  @ParameterizedTest(name = "[{index}] Validate valid File ''{0}'' with ReferenzValidator")
  @MethodSource("validErpResources")
  void shouldValidateValidResource(String file, String content) {
    val ctx = this.fhirValidator.getContext();
    val parser =
        EncodingType.guessFromContent(content)
            .chooseAppropriateParser(ctx::newXmlParser, ctx::newJsonParser);
    val resource = parser.parseResource(content);
    assertTrue(this.fhirValidator.isValid(resource));
  }
}
