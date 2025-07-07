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

package de.gematik.bbriccs.fhir.de.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddressBuilderTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Friedrichstr. 60",
        "Friedrichstraße 38",
        "Friedrichstraße 38/40",
        "Friedrichstr. 60 3 OG",
        "Friedrichstr.",
        "Kolmarer Str. 928",
        "Nikolaus-Ehlen-Weg 21b",
        "Nisbléstr 87",
        "Adalbertstr. 198 Apt. 191"
      })
  void testFixedStreetNames(String street) {
    val city = "Berlin";
    val postal = "10117";
    val builder = AddressBuilder.ofPhysicalType().city(city).postal(postal);
    assertDoesNotThrow(() -> builder.street(street).build());
  }

  @Test
  void testRandomStreetNames() {
    val city = "Berlin";
    val postal = "10117";
    val builder = AddressBuilder.ofBothTypes().city(city).postal(postal);
    for (int i = 0; i < 10; i++) {
      val rndStreet = FakerBrick.getGerman().streetName();
      assertDoesNotThrow(() -> builder.street(rndStreet).build());
    }
  }

  @Test
  void shouldThrowOnInvalidStreetNamePattern() {
    val city = "Berlin";
    val postal = "10117";
    val invalidStreet = "13te Straße";
    val builder =
        AddressBuilder.ofPostalType()
            .country(Country.A)
            .city(city)
            .postal(postal)
            .street(invalidStreet);
    assertThrows(BuilderException.class, builder::build);
  }
}
