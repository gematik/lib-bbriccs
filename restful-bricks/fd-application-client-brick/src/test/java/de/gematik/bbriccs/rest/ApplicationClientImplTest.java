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

package de.gematik.bbriccs.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.rest.exceptions.UnexpectedResponseTypeError;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ApplicationClientImplTest {

  static Stream<Arguments> shouldPerformSimpleGetRequest() {
    return Stream.of(new TestPutRequest(), new TestGetRequest()).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldPerformSimpleGetRequest(ApplicationRequest<TestAppData, ErrorDataDefault> request) {
    val httpClient = mock(HttpBClient.class);
    val responsePayload = """
        {"message": "Hello World!"}
        """;
    val httpResponse = HttpBResponse.status(200).withPayload(responsePayload);
    when(httpClient.send(any())).thenReturn(httpResponse);

    val applicationClient = ApplicationClient.using(httpClient).withSimpleObjectMapper();

    val payload = assertDoesNotThrow(() -> applicationClient.requestExpectedOrThrow(request));
    assertNotNull(payload);
    assertEquals("Hello World!", payload.message);

    // just for coverage
    assertEquals(HttpVersion.HTTP_1_1, request.version());
  }

  @Test
  void shouldPerformSimplePostRequest() {
    val httpClient = mock(HttpBClient.class);
    val httpResponse = HttpBResponse.status(204).withoutPayload();
    when(httpClient.send(any())).thenReturn(httpResponse);

    val applicationClient = ApplicationClient.using(httpClient).withSimpleObjectMapper();

    val response = assertDoesNotThrow(() -> applicationClient.request(new TestPostRequest()));
    assertNotNull(response);
    val payload = assertDoesNotThrow(() -> response.asExpectedOrThrow());
    val payload2 = assertDoesNotThrow(() -> response.asExpectedOrThrow(RuntimeException::new));
    assertInstanceOf(ApplicationData.class, payload);
    assertInstanceOf(ApplicationData.class, payload2);
  }

  @ParameterizedTest
  @ValueSource(ints = {204, 403})
  void shouldAutoDetectError01(int responseCode) {
    val httpClient = mock(HttpBClient.class);
    val errorPayload =
        """
        {"errorCode": "test_error", "errorDetail": "Intended Test Error"}
        """;
    val httpResponse = HttpBResponse.status(responseCode).withPayload(errorPayload);
    when(httpClient.send(any())).thenReturn(httpResponse);

    val applicationClient = ApplicationClient.using(httpClient).withSimpleObjectMapper();

    val response = assertDoesNotThrow(() -> applicationClient.request(new TestPostRequest()));
    assertNotNull(response);
    val payload = assertDoesNotThrow(response::asErrorOrThrow);
    assertInstanceOf(ErrorDataDefault.class, payload);

    assertThrows(UnexpectedResponseTypeError.class, response::asExpectedOrThrow);
    assertThrows(RuntimeException.class, () -> response.asExpectedOrThrow(RuntimeException::new));
  }

  @ParameterizedTest
  @ValueSource(ints = {204, 403})
  void shouldAutoDetectError02(int responseCode) {
    val httpClient = mock(HttpBClient.class);
    val errorPayload =
        """
        {"errorCode": "test_error", "errorDetail": "Intended Test Error"}
        """;
    val httpResponse = HttpBResponse.status(responseCode).withPayload(errorPayload);
    when(httpClient.send(any())).thenReturn(httpResponse);

    val applicationClient = ApplicationClient.using(httpClient).withSimpleObjectMapper();
    val request = new TestPostRequest();
    assertThrows(
        UnexpectedResponseTypeError.class, () -> applicationClient.requestExpectedOrThrow(request));
  }

  @ParameterizedTest
  @ValueSource(ints = {204, 403})
  void shouldThrowOnUnexpectedPayload(int responseCode) {
    val httpClient = mock(HttpBClient.class);
    val responsePayload = """
        {"m": "Hello World!", "d": "Some details"}
        """;
    val httpResponse = HttpBResponse.status(responseCode).withPayload(responsePayload);
    when(httpClient.send(any())).thenReturn(httpResponse);

    val applicationClient = ApplicationClient.using(httpClient).withSimpleObjectMapper();

    val response = assertDoesNotThrow(() -> applicationClient.request(new TestPostRequest()));
    assertNotNull(response);
    val payload = assertDoesNotThrow(response::asErrorOrThrow);
    assertInstanceOf(ErrorDataDefault.class, payload);
  }

  @Data
  public static class TestAppData implements ApplicationData {
    String message;
  }

  public static class TestGetRequest extends ApplicationGetRequest<TestAppData, ErrorDataDefault> {
    protected TestGetRequest() {
      super(TestAppData.class, ErrorDataDefault.class, "a/b/c");
    }
  }

  public static class TestPutRequest extends ApplicationRequest<TestAppData, ErrorDataDefault> {
    protected TestPutRequest() {
      super(TestAppData.class, ErrorDataDefault.class, HttpRequestMethod.PUT, "a/b/c");
    }
  }

  public static class TestPostRequest
      extends ApplicationRequest<EmptyApplicationData, ErrorDataDefault> {

    protected TestPostRequest() {
      super(
          EmptyApplicationData.class,
          ErrorDataDefault.class,
          HttpRequestMethod.POST,
          "d/e/f",
          List.of(),
          "Hello World!".getBytes(StandardCharsets.UTF_8));
    }
  }
}
