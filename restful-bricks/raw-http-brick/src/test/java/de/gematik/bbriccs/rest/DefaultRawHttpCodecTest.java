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

package de.gematik.bbriccs.rest;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.exceptions.RawHttpCodecException;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DefaultRawHttpCodecTest {

  private final RawHttpCodec innerHttp = RawHttpCodec.defaultCodec();

  @Test
  void shouldDecodeValidResponse() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSAyMDEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJ1dGYtOCI/Pgo8VGFzayB4bWxucz0iaHR0cDovL2hsNy5vcmcvZmhpciI+PGlkIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRXJ4VGFza3wxLjEuMSIvPjwvbWV0YT48ZXh0ZW5zaW9uIHVybD0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9QcmVzY3JpcHRpb25UeXBlIj48dmFsdWVDb2Rpbmc+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvQ29kZVN5c3RlbS9GbG93dHlwZSIvPjxjb2RlIHZhbHVlPSIxNjAiLz48ZGlzcGxheSB2YWx1ZT0iTXVzdGVyIDE2IChBcG90aGVrZW5wZmxpY2h0aWdlIEFyem5laW1pdHRlbCkiLz48L3ZhbHVlQ29kaW5nPjwvZXh0ZW5zaW9uPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL1ByZXNjcmlwdGlvbklEIi8+PHZhbHVlIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PC9pZGVudGlmaWVyPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL0FjY2Vzc0NvZGUiLz48dmFsdWUgdmFsdWU9ImMxNTU3MzNiN2QxMzJlYWQyN2FmZmFmN2JiOTUwYWIxNzBhNWIxZWIwMzE3OWU2ZGFiYWQyYjk0ZThhM2M5M2UiLz48L2lkZW50aWZpZXI+PHN0YXR1cyB2YWx1ZT0iZHJhZnQiLz48aW50ZW50IHZhbHVlPSJvcmRlciIvPjxhdXRob3JlZE9uIHZhbHVlPSIyMDIyLTA1LTE4VDE4OjU2OjQ1LjQ2MiswMDowMCIvPjxsYXN0TW9kaWZpZWQgdmFsdWU9IjIwMjItMDUtMThUMTg6NTY6NDUuNDYyKzAwOjAwIi8+PHBlcmZvcm1lclR5cGU+PGNvZGluZz48c3lzdGVtIHZhbHVlPSJ1cm46aWV0ZjpyZmM6Mzk4NiIvPjxjb2RlIHZhbHVlPSJ1cm46b2lkOjEuMi4yNzYuMC43Ni40LjU0Ii8+PGRpc3BsYXkgdmFsdWU9IsOWZmZlbnRsaWNoZSBBcG90aGVrZSIvPjwvY29kaW5nPjx0ZXh0IHZhbHVlPSLDlmZmZW50bGljaGUgQXBvdGhla2UiLz48L3BlcmZvcm1lclR5cGU+PC9UYXNrPgo=";

    val response = innerHttp.decodeResponseB64(exampleInnerHttp);

    assertEquals(201, response.statusCode());
    assertEquals(HttpVersion.HTTP_1_1, response.version());
    assertEquals("application/fhir+xml;charset=utf-8", response.contentType());

    assertFalse(response.isEmptyBody(), "Body is not empty");
    assertTrue(
        response.bodyAsString().contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"),
        "Body contains XML");
    assertEquals(1167, response.contentLength());
  }

  @Test
  void shouldNotFailOnInvalidHttpHeaderValue() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSAyMDEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KVGVzdC1IZWFkZXI6DQpJbnZhbGlkLUhlYWRlcg0KDQo8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJ1dGYtOCI/Pg0KPFRhc2sgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPg==";
    val response = innerHttp.decodeResponseB64(exampleInnerHttp);

    assertEquals(201, response.statusCode());
    assertEquals(HttpVersion.HTTP_1_1, response.version());
    assertEquals("application/fhir+xml;charset=utf-8", response.contentType());

    assertFalse(response.isEmptyBody(), "Body is not empty");
    assertTrue(
        response.bodyAsString().contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"),
        "Body contains XML");
    assertEquals(1167, response.contentLength());
    assertFalse(response.hasHeader("Test-Header")); // which is contained in raw HTTP without value
    assertFalse(
        response.hasHeader("Invalid-Header")); // which is contained in raw HTTP without value
  }

  @Test
  void shouldDecodeWithoutBody() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSAyMDEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo";

    val response = innerHttp.decodeResponseB64(exampleInnerHttp);

    assertEquals(201, response.statusCode());
    assertEquals(HttpVersion.HTTP_1_1, response.version());
    assertEquals("application/fhir+xml;charset=utf-8", response.contentType());

    assertTrue(response.bodyAsString().isEmpty(), "Body is empty");
    assertTrue(response.isEmptyBody());
  }

  @Test
  void shouldThrowOnMissingStatusCode() {
    val exampleInnerHttp =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMSEgQ3JlYXRlZA0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJ1dGYtOCI/Pgo8VGFzayB4bWxucz0iaHR0cDovL2hsNy5vcmcvZmhpciI+PGlkIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRXJ4VGFza3wxLjEuMSIvPjwvbWV0YT48ZXh0ZW5zaW9uIHVybD0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9QcmVzY3JpcHRpb25UeXBlIj48dmFsdWVDb2Rpbmc+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvQ29kZVN5c3RlbS9GbG93dHlwZSIvPjxjb2RlIHZhbHVlPSIxNjAiLz48ZGlzcGxheSB2YWx1ZT0iTXVzdGVyIDE2IChBcG90aGVrZW5wZmxpY2h0aWdlIEFyem5laW1pdHRlbCkiLz48L3ZhbHVlQ29kaW5nPjwvZXh0ZW5zaW9uPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL1ByZXNjcmlwdGlvbklEIi8+PHZhbHVlIHZhbHVlPSIxNjAuMDAwLjEzMC45MTkuNTAxLjgzIi8+PC9pZGVudGlmaWVyPjxpZGVudGlmaWVyPjx1c2UgdmFsdWU9Im9mZmljaWFsIi8+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvTmFtaW5nU3lzdGVtL0FjY2Vzc0NvZGUiLz48dmFsdWUgdmFsdWU9ImMxNTU3MzNiN2QxMzJlYWQyN2FmZmFmN2JiOTUwYWIxNzBhNWIxZWIwMzE3OWU2ZGFiYWQyYjk0ZThhM2M5M2UiLz48L2lkZW50aWZpZXI+PHN0YXR1cyB2YWx1ZT0iZHJhZnQiLz48aW50ZW50IHZhbHVlPSJvcmRlciIvPjxhdXRob3JlZE9uIHZhbHVlPSIyMDIyLTA1LTE4VDE4OjU2OjQ1LjQ2MiswMDowMCIvPjxsYXN0TW9kaWZpZWQgdmFsdWU9IjIwMjItMDUtMThUMTg6NTY6NDUuNDYyKzAwOjAwIi8+PHBlcmZvcm1lclR5cGU+PGNvZGluZz48c3lzdGVtIHZhbHVlPSJ1cm46aWV0ZjpyZmM6Mzk4NiIvPjxjb2RlIHZhbHVlPSJ1cm46b2lkOjEuMi4yNzYuMC43Ni40LjU0Ii8+PGRpc3BsYXkgdmFsdWU9IsOWZmZlbnRsaWNoZSBBcG90aGVrZSIvPjwvY29kaW5nPjx0ZXh0IHZhbHVlPSLDlmZmZW50bGljaGUgQXBvdGhla2UiLz48L3BlcmZvcm1lclR5cGU+PC9UYXNrPgo=";

    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeResponseB64(exampleInnerHttp));
  }

  @Test
  void shouldThrowOnEmptyResponseBody() {
    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeResponse(new byte[0]));
  }

  @Test
  void shouldThrowOnMissingHttpVersion() {
    val data =
        "Q29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04CkNvbnRlbnQtTGVuZ3RoOiAwCjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+";
    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeResponseB64(data));
  }

  @Test
  void shouldThrowOnIncompleteStatusLine() {
    val data =
        "MSBkMjI1NDcwZTVmMzdkYzZiMWMzZjk1ZmJkNjUxYmM1YiBIVFRQLzEuMQ0KQ29udGVudC1UeXBlOiBhcHBsaWNhdGlvbi9maGlyK3htbDtjaGFyc2V0PXV0Zi04DQpDb250ZW50LUxlbmd0aDogMTE2Nw0KDQo8P3htbCB2ZXJzaW9uPSIxLjAiIGVuY29kaW5nPSJ1dGYtOCI/Pg==";
    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeResponseB64(data));
  }

  @Test
  void shouldGenerateValidInnerHttp() {
    val content = "content";
    val expectedInnerHttp =
        "UE9TVCBUYXNrLyRjcmVhdGUgSFRUUC8xLjENClggS2V5OiBYIFZhbHVlDQpBdXRob3JpemF0aW9uOiBCZWFyZXIgSURQX1Rva2VuDQpDb250ZW50LUxlbmd0aDogNw0KDQpjb250ZW50";
    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "Task/$create",
            List.of(
                new HttpHeader("X Key", "X Value"),
                JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token"),
                HttpHeader.forContentLength(content.length())),
            content);
    val encode = innerHttp.encode(request);
    val base64 = Base64.getEncoder().encodeToString(encode.getBytes(StandardCharsets.UTF_8));
    assertEquals(expectedInnerHttp, base64);
  }

  @Test
  void shouldGenerateValidInnerHttpWithEmptyBody() {
    val expectedInnerHttp =
        "UE9TVCBUYXNrLyRjcmVhdGUgSFRUUC8xLjENClggS2V5OiBYIFZhbHVlDQpBdXRob3JpemF0aW9uOiBCZWFyZXIgSURQX1Rva2VuDQpDb250ZW50LUxlbmd0aDogMA0KDQo=";
    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "Task/$create",
            List.of(
                new HttpHeader("X Key", "X Value"),
                JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token"),
                HttpHeader.forContentLength(0)),
            "");
    val encode = innerHttp.encode(request);
    val base64 = Base64.getEncoder().encodeToString(encode.getBytes(StandardCharsets.UTF_8));
    assertEquals(expectedInnerHttp, base64);
  }

  @Test
  void shouldEncodeSimpleResponse() {
    val responseHeaders =
        List.of(HttpHeader.forDate(LocalDateTime.now()), HttpHeader.forContentLength(90));
    val response = new HttpBResponse(HttpVersion.HTTP_1_1, 200, responseHeaders, "content");
    val rawResponse = innerHttp.encode(response);
    val response2 = assertDoesNotThrow(() -> innerHttp.decodeResponse(rawResponse));

    assertEquals(response.version(), response2.version());
    assertEquals(response.statusCode(), response2.statusCode());
    assertEquals(response.contentLength(), response2.contentLength());
    assertEquals(response.headerValue("Date"), response2.headerValue("Date"));
    assertEquals(response.bodyAsString(), response2.bodyAsString());
  }

  @Test
  void shouldEncodeSimpleRequest() {
    val requestHeaders =
        List.of(HttpHeader.forDate(LocalDateTime.now()), HttpHeader.forContentLength(90));
    val request =
        new HttpBRequest(
            HttpVersion.HTTP_1_1, HttpRequestMethod.POST, "a/b/c", requestHeaders, "content");
    val rawRequest = innerHttp.encode(request);
    val request2 = assertDoesNotThrow(() -> innerHttp.decodeRequest(rawRequest));

    assertEquals(request.version(), request2.version());
    assertEquals(request.contentLength(), request2.contentLength());
    assertEquals(request.headerValue("Date"), request2.headerValue("Date"));
    assertEquals(request.bodyAsString(), request2.bodyAsString());
  }

  @Test
  void shouldDecodeErpInnerVauResponse() {
    val content = ResourceLoader.readFileFromResource("responses/example_04.txt");
    val response = assertDoesNotThrow(() -> innerHttp.decodeResponse(content));
    assertEquals(201, response.statusCode());
    assertEquals(2, response.headers().size());
    assertEquals("1286", response.headerValue("Content-Length"));
    assertEquals(1286, response.contentLength());
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodeValidResponses(String rawResponse) {
    val response = assertDoesNotThrow(() -> innerHttp.decodeResponse(rawResponse));
    assertNotNull(response);
  }

  static Stream<Arguments> shouldDecodeValidResponses() {
    return ResourceLoader.readFilesFromDirectory("responses").stream().map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodeValidRequests(String rawRequest) {
    val request = assertDoesNotThrow(() -> innerHttp.decodeRequest(rawRequest));
    assertNotNull(request);
  }

  static Stream<Arguments> shouldDecodeValidRequests() {
    return ResourceLoader.readFilesFromDirectory("requests").stream().map(Arguments::of);
  }

  @Test
  void shouldThrowOnEmptyResponse() {
    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeResponse(""));
  }

  @Test
  void shouldThrowOnEmptyRequest() {
    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeRequest(""));
  }

  @Test
  void shouldThrowOnInvalidReturnCode() {
    val raw = ResourceLoader.readFileFromResource("responses/example_01.txt");
    val rawInvalid = raw.replace("HTTP/1.1 200 OK", "HTTP/1.1 NaN OK");
    assertThrows(RawHttpCodecException.class, () -> innerHttp.decodeResponse(rawInvalid));
  }
}
