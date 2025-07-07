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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class HttpBResponseTest {

  @Test
  void shouldNotThrowOnNullResponse01() {
    String nullBody = null;
    val response =
        HttpBResponse.status(200)
            .headers(
                StandardHttpHeaderKey.USER_AGENT.createHeader(" "),
                StandardHttpHeaderKey.ACCEPT.createHeader(null))
            .withPayload(nullBody);
    assertNotNull(response.body());
    assertEquals(0, response.body().length);
    assertTrue(response.isEmptyBody());
    assertEquals("", response.bodyAsString());
    assertFalse(response.hasHeader(StandardHttpHeaderKey.CONTENT_TYPE));
    assertFalse(response.hasHeader(StandardHttpHeaderKey.USER_AGENT));
    assertFalse(response.hasHeader(StandardHttpHeaderKey.ACCEPT));
  }

  @Test
  void shouldNotThrowOnNullResponse02() {
    String nullBody = null;
    val response = HttpBResponse.status(200).version(HttpVersion.HTTP_1_1).withPayload(nullBody);
    assertNotNull(response.body());
    assertEquals(0, response.body().length);
    assertTrue(response.isEmptyBody());
    assertEquals("", response.bodyAsString());
    assertFalse(response.hasHeader(StandardHttpHeaderKey.CONTENT_TYPE));
  }

  @Test
  void shouldNotThrowOnNullResponse03() {
    byte[] nullBody = null;
    val response = HttpBResponse.status(200).withPayload(nullBody);
    assertNotNull(response.body());
    assertEquals(0, response.body().length);
    assertTrue(response.isEmptyBody());
    assertEquals("", response.bodyAsString());
    assertFalse(response.hasHeader(StandardHttpHeaderKey.CONTENT_TYPE));
  }

  @Test
  void shouldNotThrowOnNullResponseWithoutBody() {
    val response = HttpBResponse.status(200).withoutPayload();
    assertNotNull(response.body());
    assertEquals(0, response.body().length);
    assertTrue(response.isEmptyBody());
    assertEquals("", response.bodyAsString());
    assertFalse(response.hasHeader(StandardHttpHeaderKey.CONTENT_TYPE));
  }

  @Test
  void shouldNotThrowOnEmptyBody01() {
    val emptyBody = new byte[0];
    val response = HttpBResponse.status(200).withPayload(emptyBody);
    assertNotNull(response.body());
    assertTrue(response.isEmptyBody());
    assertEquals("", response.bodyAsString());
  }

  @Test
  void shouldNotThrowOnEmptyBody02() {
    val emptyBody = new byte[0];
    val response = HttpBResponse.status(200).version(HttpVersion.HTTP_2).withPayload(emptyBody);
    assertNotNull(response.body());
    assertTrue(response.isEmptyBody());
    assertEquals("", response.bodyAsString());
  }

  @Test
  void shouldDecodeBodyAsUtf8String() {
    val response =
        HttpBResponse.status(200).withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertNotNull(response.body());
    assertFalse(response.isEmptyBody());
    assertEquals("HelloWorld", response.bodyAsString());
  }

  @Test
  void shouldNotThrowOnMissingContentType() {
    val response =
        HttpBResponse.status(200).withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertEquals("", response.contentType());
  }

  @ParameterizedTest
  @MethodSource
  void shouldNeverThrowOnMissingContentLength(HttpHeader contentLengthHeader) {
    val response =
        HttpBResponse.status(200)
            .headers(contentLengthHeader)
            .withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertEquals(0, response.contentLength());
  }

  static Stream<Arguments> shouldNeverThrowOnMissingContentLength() {
    return Stream.of(
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("0"),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("-0"),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("00"),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader(""),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("  "),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("\t"),
            StandardHttpHeaderKey.CONTENT_TYPE.createHeader(
                "text/plain")) // not content-length header at all
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowOnInvalidContentLengthValue(HttpHeader contentLengthHeader) {
    val response =
        HttpBResponse.status(200)
            .headers(contentLengthHeader)
            .withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertThrows(NumberFormatException.class, response::contentLength);
  }

  static Stream<Arguments> shouldThrowOnInvalidContentLengthValue() {
    return Stream.of(
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("zero"),
            StandardHttpHeaderKey.CONTENT_LENGTH.createHeader("null"))
        .map(Arguments::of);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "\n", "\r", "\n\r"})
  @NullSource
  void shouldReturnZeroOnEmptyContentLength(String clValue) {
    val response =
        HttpBResponse.status(200)
            .headers(StandardHttpHeaderKey.CONTENT_LENGTH.createHeader(clValue))
            .withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertFalse(response.hasHeader(StandardHttpHeaderKey.CONTENT_LENGTH));
    assertEquals(0, response.contentLength());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 100, 1000, -10})
  void shouldReturnContentLengthFromHeader(int clValue) {
    val response =
        HttpBResponse.status(200)
            .headers(new HttpHeader("Content-Length", String.valueOf(clValue)))
            .withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertEquals(clValue, response.contentLength());
  }

  @Test
  void shouldThrowOnInvalidContentLength() {
    val response =
        HttpBResponse.status(200)
            .headers(new HttpHeader("Content-Length", "Not-A-Number"))
            .withPayload("HelloWorld".getBytes(StandardCharsets.UTF_8));
    assertThrows(NumberFormatException.class, response::contentLength);
  }

  @Test
  void shouldAllowEmptyBody() {
    List<HttpHeader> headers = List.of();
    val rb = HttpBResponse.status(200).headers(headers);
    val response = assertDoesNotThrow(rb::withoutPayload);
    assertTrue(response.isEmptyBody());
  }

  @Test
  void shouldAllowBodyAsString() {
    val body = "Hello World";
    List<HttpHeader> headers = List.of();
    val rb = HttpBResponse.status(200).headers(headers);
    val response = assertDoesNotThrow(() -> rb.withPayload(body));
    assertFalse(response.isEmptyBody());
  }
}
