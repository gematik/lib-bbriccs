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

package de.gematik.bbriccs.fhir.validation.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.validation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ErrorMessageFilterTest {

  @SuppressWarnings("unchecked")
  private IValidationContext<IBaseResource> createValidationContextMock(List<String> messages) {
    val mock = (IValidationContext<IBaseResource>) mock(ValidationContext.class);

    val validationMessages =
        messages.stream()
            .map(
                m -> {
                  val svm = new SingleValidationMessage();
                  svm.setMessage(m);
                  svm.setSeverity(ResultSeverityEnum.WARNING);
                  return svm;
                })
            .collect(Collectors.toList());

    when(mock.getMessages()).thenReturn(validationMessages);

    return mock;
  }

  @ParameterizedTest
  @MethodSource
  void shouldFilterFhirCommentsByDefault(List<String> filters) {
    val emf = new ErrorMessageFilter(filters);

    val messages =
        List.of(
            "something went badly wrong!",
            "Unrecognised property '@fhir_comments' in your resource");

    val vctx = createValidationContextMock(messages);

    assertEquals(2, vctx.getMessages().size());
    emf.validateResource(vctx);
    assertEquals(1, vctx.getMessages().size());
  }

  static Stream<Arguments> shouldFilterFhirCommentsByDefault() {
    return Stream.of(Arguments.of(List.of()), null);
  }

  @Test
  void shouldFilterWithCustomMessages() {
    val emf = new ErrorMessageFilter(List.of("^something went badly wrong.*"));

    val messages =
        List.of(
            "something went badly wrong in your resources of Type Bundle xyz",
            "Unrecognised property '@fhir_comments' in your resource");
    val vctx = createValidationContextMock(messages);

    assertEquals(2, vctx.getMessages().size());
    emf.validateResource(vctx);
    assertEquals(0, vctx.getMessages().size());
  }
}
