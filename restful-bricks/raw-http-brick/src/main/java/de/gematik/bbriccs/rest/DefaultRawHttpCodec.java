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
    val statusLine = format("{0} {1} {2}", request.method(), request.urlPath(), request.version());

    return finishEncoding(statusLine, request);
  }

  @Override
  public String encode(HttpBResponse response) {
    val responsePhrase = ReasonPhrase.fromStatusCode(response.statusCode()).getReasonPhrase();
    val statusLine =
        format("{0} {1} {2}", response.version(), response.statusCode(), responsePhrase);

    return finishEncoding(statusLine, response);
  }

  private String finishEncoding(String statusLine, HttpBEntity httpEntity) {
    val ret = new StringBuilder(statusLine);
    ret.append(LINE_BREAK);
    httpEntity.headers().stream()
        .map(h -> format("{0}: {1}", h.key(), h.value()))
        .forEach(it -> ret.append(it).append(LINE_BREAK));

    ret.append(LINE_BREAK);
    if (!httpEntity.isEmptyBody()) {
      ret.append(httpEntity.bodyAsString());
    }

    log.trace(
        "Encoded {}:\n----------\n{}\n----------", httpEntity.getClass().getSimpleName(), ret);
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
    return HttpBResponse.status(statusLine.getRight())
        .version(statusLine.getLeft())
        .headers(headers)
        .withPayload(body);
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
    return HttpBRequest.method(requestMethod)
        .version(version)
        .urlPath(urlPath)
        .headers(headers)
        .withPayload(body);
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
