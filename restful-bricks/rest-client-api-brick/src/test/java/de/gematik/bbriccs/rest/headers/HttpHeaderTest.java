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

package de.gematik.bbriccs.rest.headers;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class HttpHeaderTest {

  @Test
  void shouldBuildContentTypeHeader() {
    val header = HttpHeader.forContentType(MediaType.JSON_UTF_8);
    assertEquals(HttpHeaders.CONTENT_TYPE, header.key());
  }

  @Test
  void shouldBuildContentLengthHeader() {
    val header = HttpHeader.forContentLength(42);
    assertEquals(HttpHeaders.CONTENT_LENGTH, header.key());
    assertEquals("42", header.value());
  }

  @Test
  void shouldBuildUserAgentHeader() {
    val header = HttpHeader.forUserAgent("Gematik UA");
    assertEquals(HttpHeaders.USER_AGENT, header.key());
    assertEquals("Gematik UA", header.value());
  }

  /**
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-3.3">RFC-2616</a>
   */
  @Test
  void shouldBuildRfcCompliantDateHeader() {
    val zdt = LocalDateTime.of(1994, Month.NOVEMBER, 6, 8, 49, 37).atZone(ZoneId.of("GMT"));
    val header = HttpHeader.forDate(zdt);
    assertEquals(HttpHeaders.DATE, header.key());
    assertEquals("Sun, 6 Nov 1994 08:49:37 GMT", header.value());
  }

  @Test
  void shouldBuildDateHeaderForSystemDefaultZoneId() {
    val ldt = LocalDateTime.now();
    val expectation =
        ZonedDateTime.of(ldt, ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    val header = HttpHeader.forDate(ldt);
    assertEquals(HttpHeaders.DATE, header.key());
    assertEquals(expectation, header.value());
  }

  @Test
  void shouldApplyHeaderOnBiConsumer() {
    val map = new HashMap<String, String>();
    StandardHttpHeaderKey.ACCEPT.createHeader("application/json").apply(map::put);

    assertEquals(1, map.size());
    assertTrue(map.containsKey(StandardHttpHeaderKey.ACCEPT.getKey()));
    assertEquals("application/json", map.get(StandardHttpHeaderKey.ACCEPT.getKey()));
  }

  @Test
  void shouldApplyHeaderKeyValueOnBiConsumer() {
    val map = new HashMap<String, String>();
    StandardHttpHeaderKey.ACCEPT.apply("application/json", map::put);

    assertEquals(1, map.size());
    assertTrue(map.containsKey(StandardHttpHeaderKey.ACCEPT.getKey()));
    assertEquals("application/json", map.get(StandardHttpHeaderKey.ACCEPT.getKey()));
  }

  @Test
  void shouldCreateHeadersFromMap() {
    val map = Map.of("Accept", "application/json", "Content-Type", "application/xml");
    val headers = HttpHeader.from(map);
    assertEquals(2, headers.size());
  }
}
