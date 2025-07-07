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

import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.junit.jupiter.api.Test;

class HttpBRequestTest {

  @Test
  void shouldNotThrowOnNullBody01() {
    byte[] nullBody = null;
    val request =
        HttpBRequest.get().version(HttpVersion.HTTP_1_1).urlPath("a/b/c").withPayload(nullBody);
    assertNotNull(request.body());
    assertEquals(0, request.body().length);
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldNotThrowOnNullBody02() {
    String nullBody = null;
    val request = HttpBRequest.get().urlPath("a/b/c").withPayload(nullBody);
    assertNotNull(request.body());
    assertEquals(0, request.body().length);
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldNotThrowOnEmptyBody() {
    val emptyBody = new byte[0];
    val request = HttpBRequest.patch().urlPath("a/b/c").withPayload(emptyBody);
    assertNotNull(request.body());
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldNotThrowOnEmptyBody02() {
    val request =
        HttpBRequest.get()
            .urlPath("a/b/c")
            .headers(StandardHttpHeaderKey.USER_AGENT.createHeader("bbriccs"))
            .withoutPayload();

    assertNotNull(request.body());
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldEncodeBodyAsString() {
    val body = "HelloWorld";
    val request = HttpBRequest.post().urlPath("a/b/c").withPayload(body);
    assertNotNull(request.body());
    assertFalse(request.isEmptyBody());
    assertEquals("HelloWorld", request.bodyAsString());
    assertTrue(request.getBearerToken().isEmpty());
  }

  @Test
  void shouldExtractBearerToken() {
    val body = "HelloWorld".getBytes(StandardCharsets.UTF_8);
    val request =
        HttpBRequest.put()
            .urlPath("a/b/c")
            .headers(JwtHeaderKey.AUTHORIZATION.createHeader("ABC"))
            .withPayload(body);

    val bearer = request.getBearerToken();
    assertTrue(bearer.isPresent());
    assertEquals("ABC", bearer.get());
  }

  @Test
  void shouldNotOverwriteExistingHeader() {
    val request =
        HttpBRequest.get()
            .urlPath("a/b/c")
            .headers(StandardHttpHeaderKey.USER_AGENT.createHeader("ABC"))
            .withoutPayload();

    request.addIfAbsentHeader(StandardHttpHeaderKey.USER_AGENT.createHeader("XYZ"));

    assertTrue(request.hasHeader(StandardHttpHeaderKey.USER_AGENT));
    assertEquals(1, request.headerValues(StandardHttpHeaderKey.USER_AGENT).size());
    assertEquals("ABC", request.headerValue(StandardHttpHeaderKey.USER_AGENT));
  }

  @Test
  void shouldSetAbsentHeader() {
    val request = HttpBRequest.get().urlPath("a/b/c").withoutPayload();

    request.addIfAbsentHeader(StandardHttpHeaderKey.USER_AGENT.createHeader("XYZ"));

    assertTrue(request.hasHeader(StandardHttpHeaderKey.USER_AGENT));
    assertEquals(1, request.headerValues(StandardHttpHeaderKey.USER_AGENT).size());
    assertEquals("XYZ", request.headerValue(StandardHttpHeaderKey.USER_AGENT));
  }

  @Test
  void shouldRemoveExistingHeader() {
    val request =
        HttpBRequest.get()
            .urlPath("a/b/c")
            .headers(StandardHttpHeaderKey.USER_AGENT.createHeader("ABC"))
            .withoutPayload();

    request.addIfAbsentHeader(StandardHttpHeaderKey.USER_AGENT.createHeader(""));

    assertFalse(request.hasHeader(StandardHttpHeaderKey.USER_AGENT));
    assertEquals(0, request.headerValues(StandardHttpHeaderKey.USER_AGENT).size());
    assertEquals("", request.headerValue(StandardHttpHeaderKey.USER_AGENT));
  }
}
