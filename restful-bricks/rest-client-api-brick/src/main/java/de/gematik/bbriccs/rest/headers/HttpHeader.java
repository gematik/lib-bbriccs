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

package de.gematik.bbriccs.rest.headers;

import static java.text.MessageFormat.format;

import com.google.common.net.MediaType;
import de.gematik.bbriccs.rest.HttpHeaderKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

  @Override
  public String toString() {
    return format("{0}: {1}", key, value);
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

  public static HttpHeader accept(MediaType mediaType) {
    return accept(mediaType.toString());
  }

  public static HttpHeader accept(String mediaType) {
    return StandardHttpHeaderKey.ACCEPT.createHeader(mediaType);
  }

  public static HttpHeader acceptCharsetUtf8() {
    return acceptCharset(StandardCharsets.UTF_8);
  }

  public static HttpHeader acceptCharset(Charset charset) {
    return StandardHttpHeaderKey.ACCEPT_CHARSET.createHeader(charset.name().toLowerCase());
  }

  public static HttpHeader forUserAgent(String userAgent) {
    return StandardHttpHeaderKey.USER_AGENT.createHeader(userAgent);
  }

  public static HttpHeader forCurrentDate() {
    return forDate(ZonedDateTime.now());
  }

  public static HttpHeader forDate(LocalDateTime dateTime) {
    return forDate(dateTime.atZone(ZoneId.systemDefault()));
  }

  public static HttpHeader forDate(ZonedDateTime dateTime) {
    return forDate(dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
  }

  public static HttpHeader forDate(String date) {
    return from(StandardHttpHeaderKey.DATE, date);
  }

  public static HttpHeader from(HttpHeaderKey key, String value) {
    return from(key.getKey(), value);
  }

  public static HttpHeader from(String key, String value) {
    return new HttpHeader(key, value);
  }

  public static List<HttpHeader> from(Map<String, String> headerMap) {
    return headerMap.entrySet().stream()
        .map(es -> new HttpHeader(es.getKey(), es.getValue()))
        .toList();
  }
}
