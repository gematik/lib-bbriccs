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

package de.gematik.bbriccs.gid.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.gid.GidScopeErp;
import de.gematik.bbriccs.gid.exception.GidClientConfigurationException;
import de.gematik.bbriccs.idp.data.Scope;
import java.util.Base64;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class GidAuthenticationConfigTest {

  private static final byte[] TOKEN_KEY = "0123456789abcdef0123456789abcdef".getBytes();

  private static GidAuthenticationConfig.Builder completeBuilder() {
    return GidAuthenticationConfig.builder()
        .fedAuthEndpoint("https://example.org/fedauth")
        .tokenEndpoint("https://example.org/token")
        .sekIdpIss("https://example.org/idp")
        .clientId("example-client-id")
        .redirectUri("https://redirect.example.org/callback")
        .codeChallenge("test-code-challenge")
        .codeVerifier("test-code-verifier")
        .tokenKey(TOKEN_KEY)
        .kvnr("X110411675")
        .scope(Scope.openId(GidScopeErp.E_REZEPT));
  }

  @Test
  void builderShouldExposeConfiguredScopeValue() {
    val config = completeBuilder().keyVerifier("test-key-verifier").build();

    assertEquals("openid e-rezept", config.getScopeValue());
    assertEquals("test-key-verifier", config.getKeyVerifier());
  }

  @Test
  void keyVerifierShouldDefaultToCodeVerifier() {
    val config = completeBuilder().build();

    assertEquals("test-code-verifier", config.getKeyVerifier());
  }

  @Test
  void buildShouldListAllMissingValues() {
    val builder = GidAuthenticationConfig.builder();
    val ex = assertThrows(GidClientConfigurationException.class, builder::build);

    val expected =
        List.of(
            "fedAuthEndpoint",
            "tokenEndpoint",
            "sekIdpIss",
            "clientId",
            "redirectUri",
            "scope",
            "kvnr",
            "codeChallenge",
            "codeVerifier",
            "tokenKey");
    assertEquals(
        "Missing required GidAuthenticationConfig values: " + String.join(", ", expected),
        ex.getMessage());
  }

  @Test
  void buildShouldReportOnlyTheMissingValue() {
    val builder = completeBuilder().tokenKey(new byte[0]);
    val ex = assertThrows(GidClientConfigurationException.class, builder::build);

    assertTrue(ex.getMessage().endsWith(": tokenKey"), ex.getMessage());
    assertFalse(ex.getMessage().contains("clientId"));
  }

  @ParameterizedTest
  @EmptySource
  @NullSource
  void buildShouldReportMissingTokenKey(String tokenKey) {
    val builder = completeBuilder().tokenKey(tokenKey);
    val ex = assertThrows(GidClientConfigurationException.class, builder::build);

    assertTrue(ex.getMessage().endsWith(": tokenKey"), ex.getMessage());
  }

  @Test
  void buildShouldRejectBlankEndpoint() {
    val builder = completeBuilder().fedAuthEndpoint("  ");
    val ex = assertThrows(GidClientConfigurationException.class, builder::build);

    assertTrue(ex.getMessage().endsWith(": fedAuthEndpoint"), ex.getMessage());
  }

  @Test
  void buildShouldRejectEmptyScope() {
    val builder =
        GidAuthenticationConfig.builder()
            .fedAuthEndpoint("https://example.org/fedauth")
            .tokenEndpoint("https://example.org/token")
            .sekIdpIss("https://example.org/idp")
            .clientId("example-client-id")
            .redirectUri("https://redirect.example.org/callback")
            .codeChallenge("test-code-challenge")
            .codeVerifier("test-code-verifier")
            .kvnr("X110411675")
            .tokenKey(Base64.getUrlEncoder().encodeToString("my token key".getBytes()));

    val ex = assertThrows(GidClientConfigurationException.class, builder::build);

    assertTrue(ex.getMessage().endsWith(": scope"), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "\n"})
  @NullSource
  void shouldRejectEgkWithoutKvnr(String kvnr) {
    val builder =
        GidAuthenticationConfig.builder()
            .fedAuthEndpoint("https://example.org/fedauth")
            .tokenEndpoint("https://example.org/token")
            .sekIdpIss("https://example.org/idp")
            .clientId("example-client-id")
            .redirectUri("https://redirect.example.org/callback")
            .codeChallenge("test-code-challenge")
            .codeVerifier("test-code-verifier")
            .scope(Scope.openId(GidScopeErp.E_REZEPT))
            .kvnr(kvnr)
            .tokenKey(Base64.getUrlEncoder().encodeToString("my token key".getBytes()));

    val ex = assertThrows(GidClientConfigurationException.class, builder::build);

    assertTrue(ex.getMessage().endsWith(": kvnr"), ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "!invalid-base64",
        "abc+/def", // standard Base64 alphabet instead of URL-safe
        "YQ=c", // padding in the middle
        "Y", // single char: not enough bits for one byte
        "ÄÖÜ", // non-ASCII
        " ",
        "\t",
        "\n"
      })
  void shouldThrowOnInvalidBase64TokenKey(String input) {
    val builder = completeBuilder();

    val ex = assertThrows(GidClientConfigurationException.class, () -> builder.tokenKey(input));

    assertTrue(ex.getMessage().contains("Invalid Base64 URL encoded tokenKey"), ex.getMessage());
  }
}
