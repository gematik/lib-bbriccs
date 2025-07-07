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

import de.gematik.bbriccs.rest.HttpBRequestImpl.HttpBRequestBuilder;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import java.util.Optional;

public interface HttpBRequest extends HttpBEntity {

  default Optional<String> getBearerToken() {
    return this.headers().stream()
        .filter(h -> h.key().equalsIgnoreCase(JwtHeaderKey.AUTHORIZATION.getKey()))
        .map(HttpHeader::value)
        .map(v -> v.replace("Bearer ", ""))
        .findFirst();
  }

  HttpRequestMethod method();

  String urlPath();

  byte[] body();

  static HttpBRequestBuilder get() {
    return method(HttpRequestMethod.GET);
  }

  static HttpBRequestBuilder post() {
    return method(HttpRequestMethod.POST);
  }

  static HttpBRequestBuilder put() {
    return method(HttpRequestMethod.PUT);
  }

  static HttpBRequestBuilder patch() {
    return method(HttpRequestMethod.PATCH);
  }

  static HttpBRequestBuilder delete() {
    return method(HttpRequestMethod.DELETE);
  }

  static HttpBRequestBuilder method(HttpRequestMethod method) {
    return new HttpBRequestBuilder(method);
  }
}
