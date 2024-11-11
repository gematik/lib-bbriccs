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

package de.gematik.bbriccs.rest;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class HttpBRequestTest {

  @Test
  void shouldNotThrowOnNullBody01() {
    byte[] nullBody = null;
    val request =
        new HttpBRequest(HttpVersion.HTTP_1_1, HttpRequestMethod.GET, "a/b/c", List.of(), nullBody);
    assertNotNull(request.body());
    assertEquals(0, request.body().length);
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldNotThrowOnNullBody02() {
    String nullBody = null;
    val request = new HttpBRequest(HttpRequestMethod.GET, "a/b/c", nullBody);
    assertNotNull(request.body());
    assertEquals(0, request.body().length);
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldNotThrowOnEmptyBody() {
    val emptyBody = new byte[0];
    val request = new HttpBRequest(HttpRequestMethod.GET, "a/b/c", emptyBody);
    assertNotNull(request.body());
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldNotThrowOnEmptyBody02() {
    val request =
        new HttpBRequest(
            HttpRequestMethod.GET,
            "a/b/c",
            StandardHttpHeaderKey.USER_AGENT.createHeader("bbriccs"),
            "");
    assertNotNull(request.body());
    assertTrue(request.isEmptyBody());
    assertEquals("", request.bodyAsString());
  }

  @Test
  void shouldEncodeBodyAsString() {
    val body = "HelloWorld";
    val request = new HttpBRequest(HttpRequestMethod.GET, "a/b/c", List.of(), body);
    assertNotNull(request.body());
    assertFalse(request.isEmptyBody());
    assertEquals("HelloWorld", request.bodyAsString());
    assertTrue(request.getBearerToken().isEmpty());
  }

  @Test
  void shouldExtractBearerToken() {
    val body = "HelloWorld".getBytes(StandardCharsets.UTF_8);
    val request =
        new HttpBRequest(
            HttpRequestMethod.GET, "a/b/c", JwtHeaderKey.AUTHORIZATION.createHeader("ABC"), body);

    val bearer = request.getBearerToken();
    assertTrue(bearer.isPresent());
    assertEquals("ABC", bearer.get());
  }
}
