/*
 * Copyright (Change Date see Readme) gematik GmbH
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

package de.gematik.bbriccs.rest.plugins;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class BasicHeaderProviderTest {

  @Test
  void shouldProvideFixedValueHeader() {
    val header = HttpHeader.accept("text/plain");
    val fhvp = BasicHeaderProvider.forStaticHeader(header);
    val headers = fhvp.forRequest(null); // does not touch the request anyway
    assertTrue(headers.contains(header));
  }

  @Test
  void shouldProvideDynamicDateHeader() {
    val fhvp = BasicHeaderProvider.forDate();
    val headers = fhvp.forRequest(null);
    assertNotNull(headers);
    assertFalse(headers.isEmpty());

    val header1 = headers.get(0);
    assertEquals(StandardHttpHeaderKey.DATE.getKey(), header1.key());

    assertDoesNotThrow(
        () -> ZonedDateTime.parse(header1.value(), DateTimeFormatter.RFC_1123_DATE_TIME));
  }

  @Test
  void shouldProvideDynamicXRequestIdHeader() {
    val fhvp = BasicHeaderProvider.forXRequestId();
    val headers = fhvp.forRequest(null);
    assertNotNull(headers);
    assertFalse(headers.isEmpty());

    val header1 = headers.get(0);

    assertEquals("x-request-id", header1.key());

    assertDoesNotThrow(() -> UUID.fromString(header1.value()));
  }

  @Test
  void shouldCalculateContentLengthHeader() {
    val fhvp = BasicHeaderProvider.forAutoContentLength();
    val request = HttpBRequest.get().withPayload("hello".getBytes(StandardCharsets.UTF_8));
    val headers = fhvp.forRequest(request);
    assertNotNull(headers);
    assertFalse(headers.isEmpty());

    val header1 = headers.get(0);
    assertEquals("Content-Length", header1.key());
    assertEquals("5", header1.value());
  }

  @Test
  void shouldOmitContentLengthZero() {
    val fhvp = BasicHeaderProvider.forAutoContentLength();
    val request = HttpBRequest.get().withoutPayload();
    val headers = fhvp.forRequest(request);
    assertTrue(headers.isEmpty());
  }

  @Test
  void shouldNotOmitContentLengthZero() {
    val fhvp = BasicHeaderProvider.forAutoContentLength(false);
    val request = HttpBRequest.get().withoutPayload();
    val headers = fhvp.forRequest(request);
    assertNotNull(headers);
    assertFalse(headers.isEmpty());

    val header1 = headers.get(0);
    assertEquals("Content-Length", header1.key());
    assertEquals("0", header1.value());
  }
}
