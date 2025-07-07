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

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Strings;
import lombok.val;
import org.junit.jupiter.api.Test;

class RedirectLocationTest {

  @Test
  void shouldParseExamples() {
    val input =
        """
            https://idp-ref.zentral.idp.splitdns.ti-dienste.de/auth\
            ?response_type=code\
            &scope=openid+ePA-bmt-rt\
            &nonce=ongO2U6JIOnErvd7A5GR8DFuKA2Itb6T8CH5Z4Mix2jj6oZTAvcTSl0BW7pm1q4S\
            &client_id=GEMBITMAePAe2zrxzLOR\
            &redirect_uri=https%3A%2F%2Fe4a-rt.deine-epa.de%2F\
            &code_challenge=68ZyxMleyK0WroN7IbS91B07kKc1IhLonk1_C5AbSLU\
            &code_challenge_method=S256\
            &state=bYvbvd5qFYUHPwBotBXGslFPWy1uAwVpt54bgEHTZDZ8isj4rVh9eL2nVfcxp7Im
            """;

    val redirect = assertDoesNotThrow(() -> SmartcardIdpRedirect.from(input));
    assertDoesNotThrow(redirect::getBaseUrl);
    val scopes = redirect.getScopes();
    assertEquals(2, scopes.size());
    assertTrue(scopes.contains("openid"));
    assertTrue(scopes.contains("ePA-bmt-rt"));
    assertEquals(
        "ongO2U6JIOnErvd7A5GR8DFuKA2Itb6T8CH5Z4Mix2jj6oZTAvcTSl0BW7pm1q4S",
        redirect.getNonce().value());
    assertEquals("GEMBITMAePAe2zrxzLOR", redirect.getClientId().value());
    assertEquals("https://e4a-rt.deine-epa.de/", redirect.getRedirectUri().value());
    assertEquals(
        "68ZyxMleyK0WroN7IbS91B07kKc1IhLonk1_C5AbSLU", redirect.getCodeChallenge().value());
    assertEquals(CodeChallengeMethod.S256, redirect.getCodeChallengeMethod());
    assertEquals(
        "bYvbvd5qFYUHPwBotBXGslFPWy1uAwVpt54bgEHTZDZ8isj4rVh9eL2nVfcxp7Im",
        redirect.getState().value());
  }

  @Test
  void shouldParseExamples02() {
    val input =
        """
            https://gsi-ref.dev.gematik.solutions/auth\
            ?client_id=https%3A%2F%2Fe4a-ru.deine-epa.de\
            &request_uri=urn%3Ahttps%3A%2F%2Fe4a-ru.deine-epa.de%3Aebafa5847adfd7b3\
            &state=c2VTWGNDb3VxRFB0NG5ZenY5VEFKdWlPUkNiTFZRNEdmVnd5WkZqejAwUmhpQ1ljN0FOYW1yZmh5WDNPQlFtNw\
            """;

    val redirect = assertDoesNotThrow(() -> AuthRequestRedirect.from(input));
    assertDoesNotThrow(redirect::getBaseUrl);
    assertDoesNotThrow(redirect::getRelativePath);

    assertEquals("https://e4a-ru.deine-epa.de", redirect.getClientId().value());
    assertEquals(
        "c2VTWGNDb3VxRFB0NG5ZenY5VEFKdWlPUkNiTFZRNEdmVnd5WkZqejAwUmhpQ1ljN0FOYW1yZmh5WDNPQlFtNw",
        redirect.getState().value());
    assertEquals(
        "urn:https://e4a-ru.deine-epa.de:ebafa5847adfd7b3", redirect.getRequestUri().value());
  }

  @Test
  void shouldParseExamples03() {
    val input =
        """
            https://gsi-ref.dev.gematik.solutions/auth\
            ?request_uri=urn%3Ahttps%3A%2F%2Fe4a-ru.deine-epa.de%3Afe48bbf60ca2ca7b\
            &user_id=X110411675\
            &amr_value=urn%3Atelematik%3Aauth%3AeGK\
            &acr_value=gematik-ehealth-loa-high\
            &selected_claims=urn%3Atelematik%3Aclaims%3Aprofession+urn%3Atelematik%3Aclaims%3Aid+urn%3Atelematik%3Aclaims%3Aorganization+urn%3Atelematik%3Aclaims%3Adisplay_name
            """;

    val redirect = assertDoesNotThrow(() -> RedirectLocation.asGenericRedirect(input));
    val url = assertDoesNotThrow(redirect::getBaseUrl);
    val path = assertDoesNotThrow(redirect::getRelativePath);
    assertFalse(Strings.isNullOrEmpty(url));
    assertFalse(Strings.isNullOrEmpty(path));
  }
}
