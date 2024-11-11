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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record HttpBResponse(
    HttpVersion version, int statusCode, List<HttpHeader> headers, byte[] body)
    implements HttpBEntity {

  public HttpBResponse {
    if (body == null) {
      body = new byte[0];
    }
  }

  public HttpBResponse(int statusCode, List<HttpHeader> headers, byte[] body) {
    this(HttpVersion.HTTP_1_1, statusCode, headers, body);
  }

  public HttpBResponse(int statusCode, List<HttpHeader> headers, String body) {
    this(HttpVersion.HTTP_1_1, statusCode, headers, body);
  }

  public HttpBResponse(int statusCode, List<HttpHeader> headers) {
    this(HttpVersion.HTTP_1_1, statusCode, headers, new byte[0]);
  }

  public HttpBResponse(HttpVersion version, int statusCode, List<HttpHeader> headers, String body) {
    this(
        version,
        statusCode,
        headers,
        Optional.ofNullable(body).map(b -> b.getBytes(StandardCharsets.UTF_8)).orElse(new byte[0]));
  }

  public HttpBResponse(HttpVersion version, int statusCode, List<HttpHeader> headers) {
    this(version, statusCode, headers, new byte[0]);
  }

  @Override
  @Generated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HttpBResponse that = (HttpBResponse) o;
    return statusCode == that.statusCode
        && Objects.equals(version, that.version)
        && Objects.equals(headers, that.headers)
        && Arrays.equals(body, that.body);
  }

  @Override
  @Generated
  public int hashCode() {
    int result = Objects.hash(version, statusCode, headers);
    result = 31 * result + Arrays.hashCode(body);
    return result;
  }

  @Override
  @Generated
  public String toString() {
    return "HttpResponse{"
        + "protocol='"
        + version
        + '\''
        + ", statusCode="
        + statusCode
        + ", headers="
        + headers
        + '\''
        + ", body="
        + body.length
        + " Bytes"
        + '}';
  }
}
