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

import de.gematik.bbriccs.rest.headers.HttpHeader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class HttpBResponseImpl implements HttpBResponse {

  private final HttpVersion version;
  private final int statusCode;
  private final List<HttpHeader> headers;
  private final byte[] body;

  private HttpBResponseImpl(HttpBResponseBuilder builder, byte[] body) {
    this.version = builder.version;
    this.statusCode = builder.statusCode;
    this.headers = builder.headers;
    this.body = body;
  }

  @Override
  @Generated
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HttpBResponseImpl that = (HttpBResponseImpl) o;
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

  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  public static class HttpBResponseBuilder {

    private HttpVersion version = HttpVersion.HTTP_1_1;
    private final int statusCode;
    private final List<HttpHeader> headers = new ArrayList<>();

    public HttpBResponseBuilder version(HttpVersion version) {
      this.version = version;
      return this;
    }

    public HttpBResponseBuilder headers(HttpHeader... headers) {
      this.headers.addAll(Arrays.asList(headers));
      return this;
    }

    public HttpBResponseBuilder headers(List<HttpHeader> headers) {
      this.headers.addAll(headers);
      return this;
    }

    public HttpBResponse withoutPayload() {
      return withPayload("");
    }

    public HttpBResponse withPayload(String payload) {
      if (payload == null) return withoutPayload();
      else return withPayload(payload.getBytes(StandardCharsets.UTF_8));
    }

    public HttpBResponse withPayload(byte[] payload) {
      if (payload == null) {
        payload = new byte[0];
      }

      return new HttpBResponseImpl(this, payload);
    }
  }
}
