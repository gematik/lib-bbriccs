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
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class GsiLoginData {

  // REST specific
  @Delegate private final URL original;

  // OAuth2 specific
  private final RequestUri requestUri;
  private final String userId;
  private final AmrValue amrValue;
  private final AcrValue acrValue;
  private final List<Claim> selectedClaims;

  private GsiLoginData(GsiLoginBuilder b) {
    this.original = b.original;
    this.userId = b.userId;
    this.amrValue = b.amrValue;
    this.acrValue = b.acrValue;
    this.requestUri = b.requestUri;
    this.selectedClaims = b.selectedClaims;
  }

  public String getBaseUrl() {
    return format("{0}://{1}", original.getProtocol(), original.getHost());
  }

  /**
   * @return the part of the URL excluding the base URL (authority).
   */
  public String getRelativePath() {
    return this.original.toString().replace(getBaseUrl(), "");
  }

  @Override
  public String toString() {
    return this.original.toString();
  }

  public static GsiLoginBuilder from(String location) {
    try {
      val url = new URL(location);
      return from(url);
    } catch (Exception e) {
      throw new AuthException("Invalid URL: " + location, e);
    }
  }

  public static GsiLoginBuilder from(URL location) {
    return new GsiLoginBuilder(location);
  }

  @Setter
  @Accessors(fluent = true)
  public static class GsiLoginBuilder {
    private URL original;

    // OAuth2 specific
    private RequestUri requestUri;
    private String userId;
    private AmrValue amrValue;
    private AcrValue acrValue;
    private List<Claim> selectedClaims = new ArrayList<>();

    private GsiLoginBuilder(URL original) {
      this.original = original;
    }

    public GsiLoginBuilder requestUri(String requestUri) {
      return requestUri(RequestUri.from(requestUri));
    }

    public GsiLoginBuilder requestUri(RequestUri requestUri) {
      this.requestUri = requestUri;
      return this;
    }

    public GsiLoginBuilder selectedClaims(Claim... claims) {
      this.selectedClaims.addAll(Arrays.asList(claims));
      return this;
    }

    @SneakyThrows
    private String buildQueryString() {
      val query = new HashMap<String, String>();
      Optional.ofNullable(this.userId).ifPresent(it -> query.put("user_id", it));
      Optional.ofNullable(this.amrValue).ifPresent(it -> query.put("amr_value", it.value()));
      Optional.ofNullable(this.acrValue).ifPresent(it -> query.put("acr_value", it.value()));
      Optional.ofNullable(this.requestUri).ifPresent(it -> query.put("request_uri", it.value()));
      Optional.ofNullable(this.selectedClaims)
          .filter(it -> !it.isEmpty())
          .ifPresent(
              it ->
                  query.put(
                      "selected_claims",
                      it.stream().map(Claim::value).collect(Collectors.joining(" "))));

      val qs =
          query.entrySet().stream()
              .map(
                  e ->
                      format(
                          "{0}={1}",
                          e.getKey(), URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)))
              .collect(Collectors.joining("&"));

      this.original = new URL(format("{0}?{1}", this.original, qs));
      return qs;
    }

    public GsiLoginData build() {
      // split and parse the location into tokens of key value pairs
      val query = Optional.ofNullable(this.original.getQuery()).orElseGet(this::buildQueryString);
      val redirectTokens =
          Arrays.stream(query.split("&"))
              .map(
                  kv -> {
                    val tokens = kv.split("=");
                    return Pair.of(tokens[0], tokens[1]);
                  })
              .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

      val scopeTokens =
          Optional.ofNullable(redirectTokens.get("selected_claims"))
              .map(it -> it.split("(%20|\\+| )"))
              .orElse(new String[0]);
      this.selectedClaims =
          Arrays.stream(scopeTokens)
              .map(it -> URLDecoder.decode(it, StandardCharsets.UTF_8))
              .map(Claim::from)
              .collect(Collectors.toList());

      this.requestUri =
          Optional.ofNullable(redirectTokens.get("request_uri"))
              .map(it -> URLDecoder.decode(it, StandardCharsets.UTF_8))
              .map(RequestUri::from)
              .orElse(null);

      this.userId = redirectTokens.get("user_id");
      this.amrValue = AmrValue.from(redirectTokens.get("amr_value"));
      this.acrValue = AcrValue.from(redirectTokens.get("acr_value"));

      return new GsiLoginData(this);
    }
  }
}
