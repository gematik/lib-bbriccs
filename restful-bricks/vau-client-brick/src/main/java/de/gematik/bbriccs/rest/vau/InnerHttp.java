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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.vau.exceptions.VauException;
import java.net.http.HttpClient.Version;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/** Utility-class for wrapping and unwrapping the inner-HTTP */
@Slf4j
public class InnerHttp implements RawHttpCodec {

  private static final String LINE_BREAK = "\r\n";
  private static final String DOUBLE_LINE_BREAK = LINE_BREAK + LINE_BREAK;

  private final Version httpVersion;

  public InnerHttp() {
    this.httpVersion = Version.HTTP_1_1;
  }

  @Override
  public Version httpVersion() {
    return this.httpVersion;
  }

  @Override
  public String encode(HttpBRequest request) {
    StringBuilder ret = new StringBuilder();

    ret.append(request.method())
        .append(" ")
        .append(request.urlPath())
        .append(" ")
        .append(this.getHttpVersionString())
        .append(LINE_BREAK);

    request
        .headers()
        .forEach(h -> ret.append(h.key()).append(": ").append(h.value()).append(LINE_BREAK));

    ret.append("content-length: ").append(request.body().length).append(DOUBLE_LINE_BREAK);
    if (!request.isEmptyBody()) ret.append(request.bodyAsString());
    log.trace(format("Encode inner-HTTP for Request:\n----------\n{0}\n----------", ret));

    return ret.toString();
  }

  @Override
  public HttpBResponse decode(byte[] rawResponse) {
    val data = new String(rawResponse, StandardCharsets.UTF_8);
    if (data.isBlank())
      throw new VauException(format("response is not parsable, Response: {0}", data));

    val rawHttpParts = data.split(DOUBLE_LINE_BREAK);
    val genericHttMessageLines = rawHttpParts[0].split(LINE_BREAK);
    val startLine = genericHttMessageLines[0];

    val rawHeaders = new String[genericHttMessageLines.length - 1]; // without the status line
    System.arraycopy(genericHttMessageLines, 1, rawHeaders, 0, rawHeaders.length);

    if (!this.matchesHttpVersion(startLine))
      throw new VauException(
          format(
              "http protocol does not match {0}; given in StartLine: {1}",
              this.getHttpVersionString(), startLine));

    val statusLine = startLine.substring(startLine.indexOf(this.getHttpVersionString()));
    val statusLineItems = statusLine.split(" ");

    if (statusLineItems.length < 2) throw new VauException("status line is incomplete");

    try {
      val protocol = statusLineItems[0];
      val statusCode = Integer.parseInt(statusLineItems[1]);
      val headers = parseHeader(rawHeaders);
      val body = rawHttpParts.length == 2 ? rawHttpParts[1] : "";
      return new HttpBResponse(protocol, statusCode, headers, body);
    } catch (NumberFormatException e) {
      throw new VauException(format("status code MUST be a number but is {0}", statusLineItems[1]));
    }
  }

  private List<HttpHeader> parseHeader(String[] rawHeader) {
    return Arrays.stream(rawHeader)
        .map(InnerHttp::parseHeaderLine)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private boolean matchesHttpVersion(String input) {
    return input.contains(this.getHttpVersionString());
  }

  private static Optional<HttpHeader> parseHeaderLine(String header) {
    val headerTokens = header.split(": ?", 2);
    if (headerTokens.length <= 1) {
      log.warn("header structure is invalid: " + header);
      return Optional.empty();
    }
    return Optional.of(new HttpHeader(headerTokens[0], headerTokens[1]));
  }
}
