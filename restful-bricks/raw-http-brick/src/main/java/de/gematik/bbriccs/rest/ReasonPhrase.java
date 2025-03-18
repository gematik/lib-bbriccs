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

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface ReasonPhrase {
  int getStatusCode();

  String getReasonPhrase();

  static ReasonPhrase fromStatusCode(int statusCode) {
    return Arrays.stream(ReasonPhraseEnum.values())
        .filter(reasonPhrase -> reasonPhrase.getStatusCode() == statusCode)
        .map(ReasonPhrase.class::cast)
        .findFirst()
        .orElseGet(() -> new UndefinedReasonPhrase(statusCode));
  }

  @Getter
  @RequiredArgsConstructor
  enum ReasonPhraseEnum implements ReasonPhrase {
    SC_100(100, "Continue"),
    SC_101(101, "Switching Protocols"),
    SC_200(200, "OK"),
    SC_201(201, "Created"),
    SC_202(202, "Accepted"),
    SC_203(203, "Non-Authoritative Information"),
    SC_204(204, "No Content"),
    SC_205(205, "Reset Content"),
    SC_206(206, "Partial Content"),
    SC_300(300, "Multiple Choices"),
    SC_301(301, "Moved Permanently"),
    SC_302(302, "Found"),
    SC_303(303, "See Other"),
    SC_304(304, "Not Modified"),
    SC_305(305, "Use Proxy"),
    SC_307(307, "Temporary Redirect"),
    SC_400(400, "Bad Request"),
    SC_401(401, "Unauthorized"),
    SC_402(402, "Payment Required"),
    SC_403(403, "Forbidden"),
    SC_404(404, "Not Found"),
    SC_405(405, "Method Not Allowed"),
    SC_406(406, "Not Acceptable"),
    SC_407(407, "Proxy Authentication Required"),
    SC_408(408, "Request Timeout"),
    SC_409(409, "Conflict"),
    SC_410(410, "Gone"),
    SC_411(411, "Length Required"),
    SC_412(412, "Precondition Failed"),
    SC_413(413, "Request Entity Too Large"),
    SC_414(414, "Request-URI Too Long"),
    SC_415(415, "Unsupported Media Type"),
    SC_416(416, "Requested Range Not Satisfiable"),
    SC_417(417, "Expectation Failed"),
    SC_500(500, "Internal Server Error"),
    SC_501(501, "Not Implemented"),
    SC_502(502, "Bad Gateway"),
    SC_503(503, "Service Unavailable"),
    SC_504(504, "Gateway Time-out"),
    SC_505(505, "HTTP Version Not Supported");

    private final int statusCode;
    private final String reasonPhrase;
  }

  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  class UndefinedReasonPhrase implements ReasonPhrase {
    private final int statusCode;

    @Override
    public String getReasonPhrase() {
      return "Undefined";
    }
  }
}
