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

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Converts between {@code application/x-www-form-urlencoded} strings (as used in URL query strings
 * and form POST bodies) and key/value maps.
 */
@UtilityClass
public class FormUrlEncoding {

  /**
   * Appends the parameters as query string to the given URL and returns the result as validated
   * {@link URI}. If the URL already carries a query, the parameters are appended with {@code &},
   * otherwise with {@code ?}; for empty parameters the URL is returned unchanged.
   *
   * @param url absolute URL, optionally already containing a query string
   * @param params parameters to append; encoded via {@link #encode(Map)}
   * @return the resulting URI
   * @throws IllegalArgumentException if the resulting string is not a valid URI
   */
  public static URI appendQuery(String url, Map<String, String> params) {
    if (params.isEmpty()) {
      return URI.create(url);
    }
    return URI.create(url + (url.contains("?") ? "&" : "?") + encode(params));
  }

  /**
   * Encodes the given parameters as {@code key=value&key=value}; keys and values are URL-encoded
   * and the map iteration order is preserved.
   */
  public static String encode(Map<String, String> params) {
    return params.entrySet().stream()
        .map(e -> encodeValue(e.getKey()) + "=" + encodeValue(e.getValue()))
        .collect(Collectors.joining("&"));
  }

  public static String encodeValue(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  /**
   * Decodes a {@code key=value&key=value} string into a map with URL-decoded keys and values,
   * preserving the parameter order. Parameters without {@code =} are mapped to an empty value; on
   * duplicate keys the last value wins.
   *
   * @param encoded the encoded string, may be {@code null} or blank
   * @return the decoded parameters, never {@code null}
   */
  public static Map<String, String> decode(String encoded) {
    if (StringUtils.isBlank(encoded)) {
      return new LinkedHashMap<>();
    }

    return Arrays.stream(encoded.split("&"))
        .filter(StringUtils::isNotEmpty)
        .collect(
            Collectors.toMap(
                kv -> decodeValue(keyOf(kv)),
                kv -> decodeValue(valueOf(kv)),
                (first, second) -> second,
                LinkedHashMap::new));
  }

  public static String decodeValue(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private static String keyOf(String kv) {
    val idx = kv.indexOf('=');
    return idx < 0 ? kv : kv.substring(0, idx);
  }

  private static String valueOf(String kv) {
    val idx = kv.indexOf('=');
    return idx < 0 ? "" : kv.substring(idx + 1);
  }
}
