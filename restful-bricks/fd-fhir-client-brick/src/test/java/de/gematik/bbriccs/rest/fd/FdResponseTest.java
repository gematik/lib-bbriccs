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

package de.gematik.bbriccs.rest.fd;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.rest.fd.exceptions.UnexpectedResponseResourceError;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class FdResponseTest {

  @Test
  void shouldProvideEmptyResourceOnNullBody() {
    val response = FdResponse.forPayload(Bundle.class, null).andValidationResult(null);
    assertEquals(EmptyResource.class, response.getResourceType());
  }

  @Test
  void shouldThrowOnUnexpectedNullResource() {
    val response = FdResponse.forPayload(Bundle.class, null).andValidationResult(null);
    val exception =
        assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
    assertTrue(exception.getMessage().contains("of type Bundle but received NULL"));
  }

  @Test
  void shouldDetectUnexpectedOperationOutcome() {
    val response =
        FdResponse.forPayload(Bundle.class, FhirTestResourceUtil.createOperationOutcome())
            .andValidationResult(null);
    val exception =
        assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
    assertTrue(exception.getMessage().contains("of type Bundle but received OperationOutcome"));
  }

  @Test
  void shouldProvideCorrectResourceTypeRegardlessOfExpectation() {
    val response = FdResponse.forPayload(Bundle.class, new Task()).andValidationResult(null);
    assertEquals(Task.class, response.getResourceType());
  }

  @ParameterizedTest
  @MethodSource
  void shouldProvideContentLength(HttpHeader contentLength) {
    val response =
        FdResponse.forPayload(Bundle.class, new Task())
            .withHeaders(List.of(contentLength))
            .andValidationResult(null);
    assertEquals(0, response.getContentLength());
  }

  static Stream<Arguments> shouldProvideContentLength() {
    return Stream.of(
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader(""),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader(" "),
            StandardHttpHeaderKey.CONTENT_TYPE.createHeader(
                "application/json") // no content-length at all
            )
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldThrowCustomExceptionOnUnexpected(Resource resource) {
    val response =
        FdResponse.forPayload(Task.class, resource)
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    Function<FdResponse<? extends Resource>, RuntimeException> errorFunction =
        r -> new RuntimeException("test");
    assertThrows(RuntimeException.class, () -> response.getExpectedOrThrow(errorFunction));
  }

  static Stream<Arguments> shouldThrowCustomExceptionOnUnexpected() {
    return Stream.of(new Bundle(), FhirTestResourceUtil.createOperationOutcome())
        .map(Arguments::of);
  }

  @Test
  void shouldNotThrowOnExpectedResource() {
    val resource = new Bundle();
    val response =
        FdResponse.forPayload(Bundle.class, resource)
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    Function<FdResponse<? extends Resource>, RuntimeException> errorFunction =
        r -> new RuntimeException("test");
    val r2 = assertDoesNotThrow(() -> response.getExpectedOrThrow(errorFunction));
    assertEquals(r2, resource);
  }
}
