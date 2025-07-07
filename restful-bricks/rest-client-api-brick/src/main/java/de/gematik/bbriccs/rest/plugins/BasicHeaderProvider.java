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

package de.gematik.bbriccs.rest.plugins;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BasicHeaderProvider implements RequestHeaderProvider {

  private final Function<HttpBRequest, HttpHeader> httpHeaderSupplier;

  @Override
  public HttpHeader forRequest(HttpBRequest request) {
    return httpHeaderSupplier.apply(request);
  }

  public static BasicHeaderProvider forStaticHeader(HttpHeader header) {
    return fromProvider(req -> header);
  }

  /**
   * creates a {@link RequestHeaderProvider} which will dynamically add a <a
   * href="https://developer.mozilla.org/de/docs/Web/HTTP/Reference/Headers/Date">Date Header</a> to
   * each request as {@link ZonedDateTime}
   *
   * @return a BasicHeaderProvider
   */
  public static BasicHeaderProvider forDate() {
    return fromProvider(req -> HttpHeader.forDate(ZonedDateTime.now()));
  }

  /**
   * creates a {@link BasicHeaderProvider} which will dynamically calculate the "Content-Length"
   * header for the request payload
   *
   * <p>*Attention*: Some libraries like unirest do not allow you to set this Header by hand
   *
   * @return a BasicHeaderProvider which automatically calculates the content-length header
   */
  public static BasicHeaderProvider forAutoContentLength() {
    return forAutoContentLength(true);
  }

  /**
   * creates a {@link BasicHeaderProvider} which will dynamically calculate the "Content-Length"
   * header for the request payload
   *
   * <p>*Attention*: Some libraries like unirest do not allow you to set this Header by hand
   *
   * @param skipOnEmptyBody controls if "Content-Length: 0" headers should be skipped
   * @return a BasicHeaderProvider which automatically calculates the content-length header
   */
  public static BasicHeaderProvider forAutoContentLength(boolean skipOnEmptyBody) {
    return fromProvider(
        req -> {
          val contentLength = req.body().length;
          if (contentLength == 0 && skipOnEmptyBody) return null;
          else return HttpHeader.forContentLength(contentLength);
        });
  }

  /**
   * creates a {@link BasicHeaderProvider} which will dynamically add random UUID as a
   * "x-request-id" to each request
   *
   * @return a BasicHeaderProvider
   */
  public static BasicHeaderProvider forXRequestId() {
    return fromProvider(req -> new HttpHeader("x-request-id", UUID.randomUUID().toString()));
  }

  public static BasicHeaderProvider fromProvider(
      Function<HttpBRequest, HttpHeader> headerProvider) {
    return new BasicHeaderProvider(headerProvider);
  }
}
