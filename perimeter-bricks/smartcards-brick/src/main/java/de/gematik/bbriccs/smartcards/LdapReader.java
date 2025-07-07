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

package de.gematik.bbriccs.smartcards;

import java.util.List;
import javax.security.auth.x500.X500Principal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public final class LdapReader {

  private LdapReader() {
    throw new UnsupportedOperationException();
  }

  public static SmartcardOwnerData getOwnerData(X500Principal subject) {
    return getOwnerData(subject.getName());
  }

  public static SmartcardOwnerData getOwnerData(String subject) {
    val builder = SmartcardOwnerData.builder();
    val fixedName = subject.replace("+", ",");
    val elements = fixedName.split(",");

    for (val rdn : elements) {
      val token = rdn.split("=");
      val key = token[0].trim();
      val value = token[1].trim();

      switch (key.toUpperCase()) {
        case "CN" -> builder.commonName(trimName(value));
        case "T" -> builder.title(value);
        case "GIVENNAME", "GN" -> builder.givenName(trimName(value));
        case "SURNAME" -> builder.surname(trimName(value));
        case "STREET" -> builder.street(value);
        case "POSTALCODE" -> builder.postalCode(value);
        case "O" -> builder.organization(trimTestTag(value));
        case "OU" -> builder.organizationUnit(value);
        case "L" -> builder.locality(value);
        case "C" -> builder.country(value);
        default -> log.trace("ignore key {} with value {} in subject {}", key, value, subject);
      }
    }

    return builder.build();
  }

  private static String trimTestTag(String input) {
    return input.replace("TEST-ONLY", "").trim();
  }

  private static String trimName(String input) {
    var output = trimTestTag(input);

    // exclude these tags from the name
    // Note: "Arzt " has intentional space at the end to avoid matching "Arztpraxis"
    val excluded = List.of("Arzt ", "ARZT", "Apotheker/in", "APO");
    for (val tag : excluded) {
      output = output.replace(tag, "");
    }

    return output.trim();
  }
}
