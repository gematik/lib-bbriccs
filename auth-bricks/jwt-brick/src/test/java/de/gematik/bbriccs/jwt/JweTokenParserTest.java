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

package de.gematik.bbriccs.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import javax.crypto.AEADBadTagException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class JweTokenParserTest {

  private final JweTokenParser subject = new JweTokenParser();

  @Test
  void shouldExtractAccessTokenFromTokenResponse() {
    var response = "{\"access_token\":\"abc.def.ghi.jkl.mno\"}";

    assertEquals("abc.def.ghi.jkl.mno", subject.extractAccessToken(response));
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"token_type\":\"Bearer\"}", "{\"access_token\":\"\"}", "hello world"})
  @NullSource
  @EmptySource
  void shouldThrowExceptionWhenAccessTokenIsMissing(String input) {
    assertThrows(JweTokenParserException.class, () -> subject.extractAccessToken(input));
  }

  @Test
  void shouldExtractNestedJwtFromPayload() {
    val payload = "{\"njwt\":\"header.payload.signature\"}";

    assertEquals("header.payload.signature", subject.extractNestedJwt(payload));
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"someOtherField\":\"value\"}"})
  @NullSource
  @EmptySource
  void shouldThrowWhenNestedJwtIsMissing(String payload) {
    assertThrows(JweTokenParserException.class, () -> subject.extractNestedJwt(payload));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "hello world with some dots.....",
        "a.b.c",
        "a.b.c.d",
        "a.b.c.d.e.f",
        "hello world with some dots....."
      })
  @NullSource
  @EmptySource
  void shouldRejectInvalidCompactJweFormat(String input) {
    assertThrows(JweTokenParserException.class, () -> subject.decrypt(input, new byte[32]));
  }

  @Test
  void shouldDecryptCompactJwe() {
    val tokenKey = new byte[32];
    val payload = "{\"njwt\":\"header.payload.signature\"}";
    val compactJwe = subject.encrypt(payload, tokenKey);

    assertEquals(payload, subject.decrypt(compactJwe, tokenKey));
  }

  @Test
  void shouldRejectCompactJweWithInvalidAuthenticationTag() {
    val tokenKey = new byte[32];
    tokenKey[0] = 1;
    val compactJwe = subject.encrypt("payload", tokenKey);
    val parts = compactJwe.split("\\.");
    val tag = Base64.getUrlDecoder().decode(parts[4]);
    tag[0] ^= 1;
    parts[4] = Base64.getUrlEncoder().withoutPadding().encodeToString(tag);

    assertThrows(
        AEADBadTagException.class, () -> subject.decrypt(String.join(".", parts), tokenKey));
  }
}
