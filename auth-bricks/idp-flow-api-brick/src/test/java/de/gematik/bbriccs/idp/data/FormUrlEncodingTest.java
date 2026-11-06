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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FormUrlEncodingTest {

  @Test
  void shouldEncodeParametersInInsertionOrder() {
    val params = new LinkedHashMap<String, String>();
    params.put("code", "abc");
    params.put("state", "xyz");
    params.put("grant_type", "authorization_code");

    assertEquals(
        "code=abc&state=xyz&grant_type=authorization_code", FormUrlEncoding.encode(params));
  }

  @Test
  void shouldEncodeReservedCharactersInKeysAndValues() {
    val params = new LinkedHashMap<String, String>();
    params.put("redirect_uri", "https://e4a-rt.deine-epa.de/callback?x=1&y=2");
    params.put("scope", "openid e-rezept");
    params.put("we ird=key", "ä");

    assertEquals(
        "redirect_uri=https%3A%2F%2Fe4a-rt.deine-epa.de%2Fcallback%3Fx%3D1%26y%3D2"
            + "&scope=openid+e-rezept"
            + "&we+ird%3Dkey=%C3%A4",
        FormUrlEncoding.encode(params));
  }

  @Test
  void shouldEncodeEmptyMapToEmptyString() {
    assertEquals("", FormUrlEncoding.encode(Map.of()));
  }

  @Test
  void shouldEncodeSingleValue() {
    assertEquals(
        "urn%3Atelematik%3Aauth%3AeGK", FormUrlEncoding.encodeValue("urn:telematik:auth:eGK"));
  }

  @Test
  void shouldDecodeSingleValue() {
    assertEquals(
        "urn:telematik:auth:eGK", FormUrlEncoding.decodeValue("urn%3Atelematik%3Aauth%3AeGK"));
  }

  @Test
  void shouldDecodeParametersInOrder() {
    val decoded = FormUrlEncoding.decode("code=abc&state=xyz&grant_type=authorization_code");

    assertEquals(List.of("code", "state", "grant_type"), List.copyOf(decoded.keySet()));
    assertEquals("abc", decoded.get("code"));
    assertEquals("xyz", decoded.get("state"));
    assertEquals("authorization_code", decoded.get("grant_type"));
  }

  @Test
  void shouldDecodePercentAndPlusEncodedValues() {
    val decoded =
        FormUrlEncoding.decode(
            "redirect_uri=https%3A%2F%2Fe4a-rt.deine-epa.de%2F&scope=openid+ePA-bmt-rt"
                + "&selected_claims=urn%3Atelematik%3Aclaims%3Aid%20urn%3Atelematik%3Aclaims%3Aprofession");

    assertEquals("https://e4a-rt.deine-epa.de/", decoded.get("redirect_uri"));
    assertEquals("openid ePA-bmt-rt", decoded.get("scope"));
    assertEquals(
        "urn:telematik:claims:id urn:telematik:claims:profession", decoded.get("selected_claims"));
  }

  @Test
  void shouldDecodeOnlyOnce() {
    val decoded = FormUrlEncoding.decode("v=%2520");

    assertEquals("%20", decoded.get("v"));
  }

  @Test
  void shouldKeepEqualsSignsInsideValues() {
    val decoded = FormUrlEncoding.decode("token=YWJj==&next=a=b");

    assertEquals("YWJj==", decoded.get("token"));
    assertEquals("a=b", decoded.get("next"));
  }

  @Test
  void shouldMapParametersWithoutValueToEmptyString() {
    val decoded = FormUrlEncoding.decode("flag&empty=&code=abc");

    assertEquals("", decoded.get("flag"));
    assertEquals("", decoded.get("empty"));
    assertEquals("abc", decoded.get("code"));
  }

  @Test
  void shouldLetLastDuplicateKeyWin() {
    val decoded = FormUrlEncoding.decode("a=1&a=2");

    assertEquals(1, decoded.size());
    assertEquals("2", decoded.get("a"));
  }

  @Test
  void shouldIgnoreEmptySegments() {
    val decoded = FormUrlEncoding.decode("&a=1&&b=2&");

    assertEquals(Map.of("a", "1", "b", "2"), decoded);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldDecodeBlankInputToEmptyMap(String input) {
    assertTrue(FormUrlEncoding.decode(input).isEmpty());
  }

  @Test
  void shouldAppendQueryWithQuestionMark() {
    val params = new LinkedHashMap<String, String>();
    params.put("client_id", "eRezeptApp");
    params.put("scope", "openid e-rezept");

    val uri = FormUrlEncoding.appendQuery("https://idp.example.org/fedauth", params);

    assertEquals(
        "https://idp.example.org/fedauth?client_id=eRezeptApp&scope=openid+e-rezept",
        uri.toString());
  }

  @Test
  void shouldAppendQueryWithAmpersandIfQueryAlreadyPresent() {
    val uri =
        FormUrlEncoding.appendQuery(
            "https://idp.example.org/auth?request_uri=urn%3Apar", Map.of("user_id", "X110411675"));

    assertEquals(
        "https://idp.example.org/auth?request_uri=urn%3Apar&user_id=X110411675", uri.toString());
  }

  @Test
  void shouldReturnUrlUnchangedForEmptyParams() {
    val uri = FormUrlEncoding.appendQuery("https://idp.example.org/auth?a=1", Map.of());

    assertEquals("https://idp.example.org/auth?a=1", uri.toString());
  }

  @Test
  void shouldKeepPortAndPathWhenAppendingQuery() {
    val uri = FormUrlEncoding.appendQuery("http://localhost:50373/auth", Map.of("state", "s1"));

    assertEquals("http", uri.getScheme());
    assertEquals("localhost", uri.getHost());
    assertEquals(50373, uri.getPort());
    assertEquals("/auth", uri.getPath());
    assertEquals("state=s1", uri.getRawQuery());
  }

  @Test
  void shouldEncodeReservedCharactersWhenAppendingQuery() {
    val uri =
        FormUrlEncoding.appendQuery(
            "https://idp.example.org/auth",
            Map.of("redirect_uri", "https://app.example.org/cb?x=1&y=2"));

    assertEquals(
        "redirect_uri=https%3A%2F%2Fapp.example.org%2Fcb%3Fx%3D1%26y%3D2", uri.getRawQuery());
    assertEquals(
        "https://app.example.org/cb?x=1&y=2",
        FormUrlEncoding.decode(uri.getRawQuery()).get("redirect_uri"));
  }

  @Test
  void shouldRejectInvalidUrlWhenAppendingQuery() {
    val params = Map.of("a", "1");
    assertThrows(
        IllegalArgumentException.class,
        () -> FormUrlEncoding.appendQuery("http://exa mple.org/auth", params));
  }

  @Test
  void shouldRoundTrip() {
    val params = new LinkedHashMap<String, String>();
    params.put("request_uri", "urn:https://e4a-ru.deine-epa.de:fe48bbf60ca2ca7b");
    params.put("user_id", "X110411675");
    params.put("selected_claims", "urn:telematik:claims:id urn:telematik:claims:profession");
    params.put("umlaut", "Müller & Söhne = 100%");

    assertEquals(params, FormUrlEncoding.decode(FormUrlEncoding.encode(params)));
  }
}
