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

import lombok.val;
import org.junit.jupiter.api.Test;

class GsiLoginDataTest {

  @Test
  void shouldParseExamples() {
    val input =
        """
https://gsi-ref.dev.gematik.solutions/auth\
?request_uri=urn:https://e4a-ru.deine-epa.de:fe48bbf60ca2ca7b\
&user_id=X110411675\
&amr_value=urn:telematik:auth:eGK\
&acr_value=gematik-ehealth-loa-high\
&selected_claims=urn:telematik:claims:profession urn:telematik:claims:id urn:telematik:claims:organization urn:telematik:claims:display_name\
  """
            .replaceAll(System.lineSeparator(), " ");

    val lb = assertDoesNotThrow(() -> GsiLoginData.from(input));
    val login = lb.build();
    val output = assertDoesNotThrow(login::toString);
  }

  @Test
  void shouldParseExampleWithPort() {
    val redirect =
        "http://localhost:50373/auth?client_id=dummy-client&request_uri=urn:dummy:paruri&state=dummystate";

    val parUri = RequestUri.from(redirect);

    val authReqRedirect = AuthRequestRedirect.from(redirect);
    val lb =
        GsiLoginData.from(authReqRedirect.withoutQuery())
            .requestUri(parUri)
            .userId("X110411675")
            .amrValue(GsiAmrValue.TELEMATIK_AUTH_EGK)
            .acrValue(GsiAcrValue.E_HEALTH_LOA_HIGH)
            .selectedClaims(
                TelematikClaim.PROFESSION,
                TelematikClaim.ID,
                TelematikClaim.OGANIZATION,
                TelematikClaim.DISPLAY_NAME,
                TelematikClaim.FAMILY_NAME,
                TelematikClaim.GIVEN_NAME);

    val login = lb.build();
    val output = assertDoesNotThrow(login::toString);
    assertTrue(output.contains("http://localhost:50373/auth"));
    assertEquals(
        "http://localhost:50373/auth?selected_claims=urn%3Atelematik%3Aclaims%3Aprofession+urn%3Atelematik%3Aclaims%3Aid+urn%3Atelematik%3Aclaims%3Aorganization+urn%3Atelematik%3Aclaims%3Adisplay_name+urn%3Atelematik%3Aclaims%3Afamily_name+urn%3Atelematik%3Aclaims%3Agiven_name&acr_value=gematik-ehealth-loa-high&user_id=X110411675&amr_value=urn%3Atelematik%3Aauth%3AeGK&request_uri=http%3A%2F%2Flocalhost%3A50373%2Fauth%3Fclient_id%3Ddummy-client%26request_uri%3Durn%3Adummy%3Aparuri%26state%3Ddummystate",
        output);
  }

  @Test
  void shouldBuildExample() {
    val login =
        GsiLoginData.from("https://gsi-ref.dev.gematik.solutions/auth")
            .requestUri("urn:https://e4a-ru.deine-epa.de:fe48bbf60ca2ca7b")
            .userId("X110411675")
            .selectedClaims(
                TelematikClaim.PROFESSION,
                TelematikClaim.ID,
                TelematikClaim.OGANIZATION,
                TelematikClaim.DISPLAY_NAME)
            .build();
    val output = assertDoesNotThrow(login::toString);
    System.out.println(output);
  }
}
