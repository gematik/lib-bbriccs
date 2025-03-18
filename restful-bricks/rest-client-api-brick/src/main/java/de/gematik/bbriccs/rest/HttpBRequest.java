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

import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Generated;

public record HttpBRequest(
    HttpVersion version,
    HttpRequestMethod method,
    String urlPath,
    List<HttpHeader> headers,
    byte[] body)
    implements HttpBEntity {

  public HttpBRequest {
    if (body == null) {
      body = new byte[0];
    }
  }

  public HttpBRequest(HttpVersion version, HttpRequestMethod method, String urlPath) {
    this(version, method, urlPath, List.of(), new byte[0]);
  }

  public HttpBRequest(HttpRequestMethod method, String urlPath) {
    this(HttpVersion.HTTP_1_1, method, urlPath);
  }

  public HttpBRequest(HttpRequestMethod method, String urlPath, String body) {
    this(method, urlPath, List.of(), body);
  }

  public HttpBRequest(HttpVersion version, HttpRequestMethod method, String urlPath, byte[] body) {
    this(version, method, urlPath, List.of(), body);
  }

  public HttpBRequest(HttpRequestMethod method, String urlPath, byte[] body) {
    this(HttpVersion.HTTP_1_1, method, urlPath, body);
  }

  public HttpBRequest(HttpRequestMethod method, String urlPath, HttpHeader header, String body) {
    this(method, urlPath, List.of(header), body);
  }

  public HttpBRequest(
      HttpVersion version,
      HttpRequestMethod method,
      String urlPath,
      HttpHeader header,
      byte[] body) {
    this(version, method, urlPath, List.of(header), body);
  }

  public HttpBRequest(HttpRequestMethod method, String urlPath, HttpHeader header, byte[] body) {
    this(HttpVersion.HTTP_1_1, method, urlPath, header, body);
  }

  public HttpBRequest(
      HttpVersion version,
      HttpRequestMethod method,
      String urlPath,
      List<HttpHeader> headers,
      String body) {
    this(
        version,
        method,
        urlPath,
        headers,
        Optional.ofNullable(body).map(b -> b.getBytes(StandardCharsets.UTF_8)).orElse(new byte[0]));
  }

  public HttpBRequest(
      HttpRequestMethod method, String urlPath, List<HttpHeader> headers, String body) {
    this(HttpVersion.HTTP_1_1, method, urlPath, headers, body);
  }

  public Optional<String> getBearerToken() {
    return headers.stream()
        .filter(h -> h.key().equalsIgnoreCase(JwtHeaderKey.AUTHORIZATION.getKey()))
        .map(HttpHeader::value)
        .map(v -> v.replace("Bearer ", ""))
        .findFirst();
  }

  @Override
  @Generated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HttpBRequest that = (HttpBRequest) o;
    return method == that.method
        && Objects.equals(version, that.version)
        && Objects.equals(urlPath, that.urlPath)
        && Objects.equals(headers, that.headers)
        && Arrays.equals(body, that.body);
  }

  @Override
  @Generated
  public int hashCode() {
    int result = Objects.hash(method, urlPath, headers);
    result = 31 * result + Arrays.hashCode(body);
    return result;
  }

  @Override
  @Generated
  public String toString() {
    return "HttpRequest{"
        + "method="
        + method
        + ", urlPath='"
        + urlPath
        + '\''
        + ", headers="
        + headers
        + '\''
        + ", body="
        + body.length
        + " Bytes"
        + '}';
  }
}
