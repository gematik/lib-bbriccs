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

package de.gematik.bbriccs.fhir.validation.utils;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runners.model.InitializationError;

@Slf4j
public abstract class FhirValidatingTest {

  protected ValidatorFhir fhirValidator;

  @BeforeEach
  void beforeEach() throws InitializationError {
    this.initialize();

    if (fhirValidator == null) {
      throw new InitializationError("FhirValidator was not set during initialization");
    }
  }

  protected abstract void initialize();

  protected final void printValidationResult(final ValidationResult result) {
    printValidationResult(result, m -> !m.getSeverity().equals(ResultSeverityEnum.INFORMATION));
  }

  protected final void printValidationResult(
      final ValidationResult result, Predicate<SingleValidationMessage> messageFilter) {
    if (!result.isSuccessful()) {
      // give me some hints if the encoded result is invalid
      val r =
          result.getMessages().stream()
              .filter(messageFilter)
              .map(
                  m ->
                      format(
                          "[{0} in Line {3} at {1}]: {2}",
                          m.getSeverity(),
                          m.getLocationString(),
                          m.getMessage(),
                          m.getLocationLine()))
              .collect(Collectors.joining("\n\t"));
      log.warn(
          "--- Found Validation Messages after validation: {} ---\n\t{}\n------",
          result.getMessages().stream().filter(messageFilter).count(),
          r);
    }
  }
}
