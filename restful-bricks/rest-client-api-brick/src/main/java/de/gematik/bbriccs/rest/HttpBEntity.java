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

import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

interface HttpBEntity {

  HttpVersion version();

  List<HttpHeader> headers();

  byte[] body();

  default boolean isEmptyBody() {
    return body().length == 0;
  }

  default String bodyAsString() {
    if (this.isEmptyBody()) {
      return "";
    } else {
      return new String(body(), StandardCharsets.UTF_8);
    }
  }

  default String contentType() {
    return headerValue(StandardHttpHeaderKey.CONTENT_TYPE);
  }

  default int contentLength() {
    val clValue = headerValue(StandardHttpHeaderKey.CONTENT_LENGTH);
    if (clValue.isEmpty() || clValue.isBlank()) {
      return 0;
    } else {
      return Integer.parseInt(clValue);
    }
  }

  default String headerValue(HttpHeaderKey key) {
    return headerValue(key.getKey());
  }

  default String headerValue(String key) {
    return headers().stream()
        .filter(h -> h.key().equalsIgnoreCase(key) && h.value() != null)
        .map(HttpHeader::value)
        .findFirst()
        .orElse("");
  }

  default boolean hasHeader(HttpHeaderKey key) {
    return hasHeader(key.getKey());
  }

  default boolean hasHeader(String key) {
    val value = headerValue(key);
    return StringUtils.isNotBlank(value);
  }
}
