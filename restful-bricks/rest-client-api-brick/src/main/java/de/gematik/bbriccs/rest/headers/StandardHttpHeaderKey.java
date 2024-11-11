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

import com.google.common.net.HttpHeaders;
import de.gematik.bbriccs.rest.HttpHeaderKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StandardHttpHeaderKey implements HttpHeaderKey {
  USER_AGENT(HttpHeaders.USER_AGENT),
  ACCEPT(HttpHeaders.ACCEPT),
  ACCEPT_CHARSET(HttpHeaders.ACCEPT_CHARSET),
  CONTENT_LENGTH(HttpHeaders.CONTENT_LENGTH),
  CONTENT_TYPE(HttpHeaders.CONTENT_TYPE),
  DATE(HttpHeaders.DATE);

  private final String key;

  @Override
  public HttpHeader createHeader(String value) {
    return new HttpHeader(this.key, value);
  }
}
