/*
 * Copyright (Change Date see Readme) gematik GmbH
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
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.val;

public class RedirectLocation implements ApplicationData {

  // keep the original to delegate URL methods
  @Delegate protected final URL original;
  private final Map<String, String> queryParams;

  protected RedirectLocation(URL original) {
    this.original = original;
    this.queryParams = FormUrlEncoding.decode(original.getQuery());
  }

  public Optional<String> getQueryParam(String key) {
    return Optional.ofNullable(queryParams.get(key));
  }

  public <T> Optional<T> getQueryParam(String key, Function<String, T> converter) {
    return getQueryParam(key).map(converter);
  }

  public String getQueryParamOrThrow(String key) {
    return this.getQueryParamOrThrow(key, value -> value);
  }

  public <T> T getQueryParamOrThrow(String key, Function<String, T> converter) {
    return this.getQueryParam(key, converter)
        .orElseThrow(
            () ->
                new AuthException(
                    "Missing query parameter: "
                        + key
                        + " / "
                        + findFirstErrorIn(queryParams).orElse("No error text available")));
  }

  public String getBaseUrl() {
    //    return format("{0}://{1}", original.getScheme(), original.getAuthority());  // drop in
    // once moved to URI
    return format("{0}://{1}", original.getProtocol(), original.getHost());
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

  @SneakyThrows
  public static RedirectLocation asGenericRedirect(String location) {
    return from(location, RedirectLocation::new);
  }

  protected static Optional<String> findFirstErrorIn(Map<String, String> queryParams) {
    return queryParams.entrySet().stream()
        .filter(entry -> entry.getKey().toLowerCase().contains("error"))
        .map(Map.Entry::getValue)
        .findFirst();
  }

  protected static <R extends RedirectLocation> R from(
      String location, Function<URL, R> constructor) {
    try {
      val url = URI.create(location).toURL();
      return from(url, constructor);
    } catch (Exception e) {
      throw new AuthException("Invalid URL: " + location, e);
    }
  }

  protected static <R extends RedirectLocation> R from(URL location, Function<URL, R> constructor) {
    return constructor.apply(location);
  }
}
