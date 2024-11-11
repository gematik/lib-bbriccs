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

import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.bbriccs.rest.exceptions.RawHttpCodecException;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import java.util.*;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultRawHttpCodec implements RawHttpCodec {

  private static final Pattern STATUS_LINE_REGEX_PATTERN =
      Pattern.compile("(HTTP/\\S{1,3})\\s(\\d{3})(\\w*)");
  private static final String LINE_BREAK = "\r\n";
  private static final String DOUBLE_LINE_BREAK = LINE_BREAK + LINE_BREAK;

  @Override
  public String encode(HttpBRequest request) {
    StringBuilder ret = new StringBuilder();

    ret.append(request.method())
        .append(" ")
        .append(request.urlPath())
        .append(" ")
        .append(request.version())
        .append(LINE_BREAK);

    request
        .headers()
        .forEach(h -> ret.append(h.key()).append(": ").append(h.value()).append(LINE_BREAK));

    ret.append(LINE_BREAK); // separator between the headers and the body
    if (!request.isEmptyBody()) {
      ret.append(request.bodyAsString());
    }
    log.trace("Encoded HTTP Request:\n----------\n{}\n----------", ret);

    return ret.toString();
  }

  @Override
  public String encode(HttpBResponse response) {
    StringBuilder ret = new StringBuilder();

    ret.append(response.version().version)
        .append(" ")
        .append(response.statusCode())
        .append(" ")
        .append(ReasonPhrase.fromStatusCode(response.statusCode()).getReasonPhrase())
        .append(LINE_BREAK);

    response
        .headers()
        .forEach(h -> ret.append(h.key()).append(": ").append(h.value()).append(LINE_BREAK));

    if (!response.isEmptyBody()) {
      // only a single line break because we have already one from the last header
      ret.append(LINE_BREAK).append(response.bodyAsString());
    }
    log.trace("Encoded HTTP Response:\n----------\n{}\n----------", ret);

    return ret.toString();
  }

  @Override
  public HttpBResponse decodeResponse(String rawResponse) {
    if (Strings.isNullOrEmpty(rawResponse)) {
      throw new RawHttpCodecException(
          format("response is not parsable, Response: {0}", rawResponse));
    }

    val rawHttpParts = rawResponse.split(DOUBLE_LINE_BREAK);
    val genericHttMessageLines = rawHttpParts[0].split(LINE_BREAK);
    val rawStatusLine = genericHttMessageLines[0];

    val rawHeaders = new String[genericHttMessageLines.length - 1]; // without the status line
    System.arraycopy(genericHttMessageLines, 1, rawHeaders, 0, rawHeaders.length);

    val statusLine = splitStatusLine(rawStatusLine);
    val headers = parseHeader(rawHeaders);
    val body = rawHttpParts.length == 2 ? rawHttpParts[1] : "";
    return new HttpBResponse(statusLine.getKey(), statusLine.getRight(), headers, body);
  }

  @Override
  public HttpBRequest decodeRequest(String rawRequest) {
    if (Strings.isNullOrEmpty(rawRequest)) {
      throw new RawHttpCodecException(format("response is not parsable, Request: {0}", rawRequest));
    }

    val rawHttpParts = rawRequest.split(DOUBLE_LINE_BREAK);
    val genericHttMessageLines = rawHttpParts[0].split(LINE_BREAK);
    val statusLine = genericHttMessageLines[0];
    val rawStatusLineTokens = statusLine.split(" ");
    val requestMethod = HttpRequestMethod.valueOf(rawStatusLineTokens[0]);
    val urlPath = rawStatusLineTokens[1];
    val version =
        HttpVersion.optionalFromString(rawStatusLineTokens[2]).orElse(HttpVersion.HTTP_1_1);

    val rawHeaders = new String[genericHttMessageLines.length - 1]; // without the status line
    System.arraycopy(genericHttMessageLines, 1, rawHeaders, 0, rawHeaders.length);

    val headers = parseHeader(rawHeaders);
    val body = rawHttpParts.length == 2 ? rawHttpParts[1] : "";
    return new HttpBRequest(version, requestMethod, urlPath, headers, body);
  }

  private List<HttpHeader> parseHeader(String[] rawHeader) {
    return Arrays.stream(rawHeader)
        .map(DefaultRawHttpCodec::parseHeaderLine)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Pair<HttpVersion, Integer> splitStatusLine(String statusLine) {
    val matcher = STATUS_LINE_REGEX_PATTERN.matcher(statusLine);
    if (matcher.find()) {
      val version = HttpVersion.optionalFromString(matcher.group(1)).orElse(HttpVersion.HTTP_1_1);
      val statusCode =
          Optional.ofNullable(matcher.group(2))
              .map(sc -> Integer.parseInt(sc.trim()))
              // should basically never happen because of the regex pattern
              .orElseThrow(() -> new RawHttpCodecException("HTTP status code is missing"));

      return Pair.of(version, statusCode);
    }
    throw new RawHttpCodecException("HTTP status line is invalid or incomplete");
  }

  private static Optional<HttpHeader> parseHeaderLine(String header) {
    val headerTokens = header.split(": ?", 2);
    if (headerTokens.length <= 1) {
      log.warn("header structure is invalid: {}", header);
      return Optional.empty();
    }
    return Optional.of(new HttpHeader(headerTokens[0], headerTokens[1]));
  }
}
