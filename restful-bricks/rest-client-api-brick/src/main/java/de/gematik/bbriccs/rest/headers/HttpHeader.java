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

import com.google.common.net.MediaType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public record HttpHeader(String key, String value) {

  public void apply(BiConsumer<String, String> headerConsumer) {
    headerConsumer.accept(key, value);
  }

  public static HttpHeader forContentLength(int length) {
    return StandardHttpHeaderKey.CONTENT_LENGTH.createHeader(String.valueOf(length));
  }

  public static HttpHeader forContentType(MediaType mediaType) {
    return forContentType(mediaType.toString());
  }

  public static HttpHeader forContentType(String type) {
    return StandardHttpHeaderKey.CONTENT_TYPE.createHeader(type);
  }

  public static HttpHeader forUserAgent(String userAgent) {
    return StandardHttpHeaderKey.USER_AGENT.createHeader(userAgent);
  }

  public static HttpHeader forDate(LocalDateTime dateTime) {
    return forDate(dateTime.atZone(ZoneId.systemDefault()));
  }

  public static HttpHeader forDate(ZonedDateTime dateTime) {
    return forDate(dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
  }

  public static HttpHeader forDate(String date) {
    return StandardHttpHeaderKey.DATE.createHeader(date);
  }

  public static List<HttpHeader> from(Map<String, String> headerMap) {
    return headerMap.entrySet().stream()
        .map(es -> new HttpHeader(es.getKey(), es.getValue()))
        .toList();
  }
}
