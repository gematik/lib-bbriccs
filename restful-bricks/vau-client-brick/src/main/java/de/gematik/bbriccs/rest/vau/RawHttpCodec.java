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

package de.gematik.bbriccs.rest.vau;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import java.net.http.HttpClient.Version;
import java.util.Base64;

public interface RawHttpCodec {

  Version httpVersion();

  default String getHttpVersionString() {
    return switch (this.httpVersion()) {
      case HTTP_1_1 -> "HTTP/1.1";
      case HTTP_2 -> "HTTP/2";
    };
  }

  /**
   * Encode the given command to a String representing an HTTP-Request according to RFC standards.
   * This encoded "inner-HTTP Request" will then be encrypted VAU and transported as an "outer-HTTP
   * Request"
   *
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc2616">RFC-2616</a>
   * @param request holding the internal data of the "inner-HTTP Request"
   * @return the VauRequest encoded as plain string representing the HTTP-Request according to RFC
   */
  String encode(HttpBRequest request);

  default HttpBResponse decode(String b64RawResponse) {
    return decode(Base64.getDecoder().decode(b64RawResponse));
  }

  HttpBResponse decode(byte[] rawResponse);
}
