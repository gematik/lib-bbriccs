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

package de.gematik.bbriccs.fhir;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.exceptions.FhirValidationException;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import java.util.Arrays;
import java.util.List;
import lombok.*;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.*;

class ValidationResultHelperTest {

  @Test
  void coverPrivateConstructor() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ValidationResultHelper.class));
  }

  @Test
  void shouldThrowOnBaseInvalidResult() {
    val vr = createFailingValidationResult();
    assertThrows(
        FhirValidationException.class,
        () -> ValidationResultHelper.throwOnInvalidValidationResult(vr));
  }

  @Test
  void shouldNotThrowOnBaseValidResult() {
    val vr = createEmptyValidationResult();
    assertDoesNotThrow(() -> ValidationResultHelper.throwOnInvalidValidationResult(vr));
  }

  @Test
  void shouldThrowOnConcreteInvalidResult() {
    val vr = createFailingValidationResult();
    assertThrows(
        FhirValidationException.class,
        () -> ValidationResultHelper.throwOnInvalidValidationResult(Bundle.class, vr));
  }

  @Test
  void shouldThrowOnManyDifferentErrorMessages() {
    val vr = createFailingValidationResultWithAllSeverities();
    val exception =
        assertThrows(
            FhirValidationException.class,
            () -> ValidationResultHelper.throwOnInvalidValidationResult(Bundle.class, vr));
    val message = exception.getMessage();
    assertTrue(message.contains("1 errors"));
    assertTrue(message.contains("1 warnings"));
  }

  @Test
  void shouldNotThrowOnConcreteValidResult() {
    val vr = createEmptyValidationResult();
    assertDoesNotThrow(
        () -> ValidationResultHelper.throwOnInvalidValidationResult(Bundle.class, vr));
  }

  private static ValidationResult createEmptyValidationResult() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(true);
    when(vr.getMessages()).thenReturn(List.of());
    return vr;
  }

  private static ValidationResult createFailingValidationResult() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);
    val errorMessage = new SingleValidationMessage();
    errorMessage.setMessage("mock error message");
    errorMessage.setSeverity(ResultSeverityEnum.ERROR);
    when(vr.getMessages()).thenReturn(List.of(errorMessage));
    return vr;
  }

  private static ValidationResult createFailingValidationResultWithAllSeverities() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);

    val errorMessages =
        Arrays.stream(ResultSeverityEnum.values)
            .map(
                severity -> {
                  val svm = new SingleValidationMessage();
                  svm.setMessage(format("mock {0} message", severity.getCode()));
                  svm.setSeverity(severity);
                  return svm;
                })
            .toList();

    when(vr.getMessages()).thenReturn(errorMessages);
    return vr;
  }
}
