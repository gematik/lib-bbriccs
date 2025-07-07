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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public abstract class ApplicationRequest<P extends ApplicationData, E extends ErrorData>
    implements HttpBRequest {

  private final Class<P> responseType;
  private final Class<E> errorType;
  private final HttpRequestMethod method;
  private final String urlPath;
  private final List<HttpHeader> headers = new ArrayList<>();
  private final byte[] body;

  protected ApplicationRequest(
      Class<P> responseType, Class<E> errorType, HttpRequestMethod method, String urlPath) {
    this(responseType, errorType, method, urlPath, List.of(), new byte[0]);
  }

  protected ApplicationRequest(
      Class<P> responseType,
      Class<E> errorType,
      HttpRequestMethod method,
      String urlPath,
      List<HttpHeader> headers) {
    this(responseType, errorType, method, urlPath, headers, new byte[0]);
  }

  protected ApplicationRequest(
      Class<P> responseType,
      Class<E> errorType,
      HttpRequestMethod method,
      String urlPath,
      List<HttpHeader> headers,
      byte[] body) {
    this.responseType = responseType;
    this.errorType = errorType;
    this.method = method;
    this.urlPath = urlPath;
    this.headers.addAll(headers);
    this.body = body;
  }

  @Override
  public HttpVersion version() {
    return HttpVersion.HTTP_1_1; // overwrite if you need http 2.0
  }

  public Optional<Function<HttpBResponse, P>> customDecoder() {
    return Optional.empty();
  }
}
