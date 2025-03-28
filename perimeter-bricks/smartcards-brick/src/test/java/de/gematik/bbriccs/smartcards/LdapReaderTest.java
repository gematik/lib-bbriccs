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
 */

package de.gematik.bbriccs.smartcards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LdapReaderTest {

  private static Stream<Arguments> getReferenceOwnerData() {
    return Stream.of(
        Arguments.of(
            "GIVENNAME=Bernd + SURNAME=Claudius + SERIALNUMBER=16.80276001011699910102 + CN=Arzt"
                + " Bernd Claudius TEST-ONLY, C=DE",
            "Bernd",
            "Claudius",
            "Arzt Bernd Claudius TEST-ONLY"),
        Arguments.of(
            "SURNAME=Gunther + GIVENNAME=Gündüla + SERIALNUMBER=11.80276001081699900578 + CN=Dr."
                + " med. Gündüla Gunther ARZT TEST-ONLY, C=DE + T = Dr. med. + STREET ="
                + " Friedrichstrasse 136 + POSTALCODE = 10117 + OU = someOrganizationUnit + L ="
                + " locality",
            "Gündüla",
            "Gunther",
            "Dr. med. Gündüla Gunther ARZT TEST-ONLY",
            "Dr. med.",
            "Friedrichstrasse 136",
            "10117",
            "someOrganizationUnit",
            "locality"),
        Arguments.of(
            "GIVENNAME=Amanda + SURNAME=Albrecht + SERIALNUMBER=11.80276001081699900579 + CN=Dr."
                + " Amanda Albrecht APO TEST-ONLY, C=DE",
            "Amanda",
            "Albrecht",
            "Dr. Amanda Albrecht APO TEST-ONLY"),
        Arguments.of(
            "CN=Arztpraxis Bernd Claudius TEST-ONLY, GIVENNAME=Bernd, SURNAME=Claudius, O=202110001"
                + " NOT-VALID, C=DE",
            "Bernd",
            "Claudius",
            "Arztpraxis Bernd Claudius TEST-ONLY"));
  }

  @ParameterizedTest
  @MethodSource
  void getReferenceOwnerData(String subject, String givenName, String surname, String commonName) {
    val bernd = LdapReader.getOwnerData(subject);
    assertEquals(givenName, bernd.getGivenName());
    assertEquals(surname, bernd.getSurname());
    assertEquals(commonName, bernd.getCommonName());
  }

  @Test
  void constructorShouldNotBeCallable() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(LdapReader.class));
  }
}
