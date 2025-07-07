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

package de.gematik.bbriccs.idp.data;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.idp.exception.AuthException;
import de.gematik.bbriccs.rest.ApplicationData;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

public class RedirectLocation implements ApplicationData {

  // keep the original to delegate URL methods
  @Delegate protected final URL original;
  protected final Map<String, String> queryParams;

  protected RedirectLocation(URL original) {
    this.original = original;
    this.queryParams = RedirectLocation.parseQuery(original.getQuery());
  }

  protected static Map<String, String> parseQuery(String query) {
    return Arrays.stream(query.split("&"))
        .map(
            kv -> {
              val tokens = kv.split("=");
              val key = tokens[0];
              val value = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8);
              return Pair.of(key, value);
            })
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  public Optional<String> getQueryParam(String key) {
    return Optional.ofNullable(queryParams.get(key));
  }

  public String getBaseUrl() {
    return format("{0}://{1}", original.getProtocol(), original.getHost());
  }

  @SneakyThrows
  public static RedirectLocation asGenericRedirect(String location) {
    val url = new URL(location);
    return new RedirectLocation(url);
  }

  protected static <R extends RedirectLocation> R from(
      String location, Function<URL, R> constructor) {
    try {
      val url = new URL(location);
      return from(url, constructor);
    } catch (Exception e) {
      throw new AuthException("Invalid URL: " + location, e);
    }
  }

  protected static <R extends RedirectLocation> R from(URL location, Function<URL, R> constructor) {
    return constructor.apply(location);
  }

  /**
   * @return the part of the URL excluding the base URL (authority).
   */
  public String getRelativePath() {
    return this.original.toString().replace(getBaseUrl(), "");
  }

  public String withoutQuery() {
    return this.original.toString().split("\\?")[0];
  }

  @Override
  public String toString() {
    return original.toString();
  }
}
