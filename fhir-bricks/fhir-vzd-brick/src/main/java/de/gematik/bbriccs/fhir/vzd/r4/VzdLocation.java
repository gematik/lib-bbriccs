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

package de.gematik.bbriccs.fhir.vzd.r4;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.PrimitiveType;

@Slf4j
@ResourceDef(name = "Location")
@SuppressWarnings({"java:S110"})
public class VzdLocation extends Location {

  public String getAddressString() {
    val address = this.getAddress();
    val country = address.getCountry();
    val city = address.getCity();
    val postalCode = address.getPostalCode();
    val street =
        address.getLine().stream().map(PrimitiveType::getValue).collect(Collectors.joining(" / "));

    return format("{0}, {1} {2} {3}", country, postalCode, city, street);
  }

  public String getLocationString() {
    val position = this.getPosition();
    val longitude = position.getLongitude();
    val latitude = position.getLatitude();

    if (longitude != null && latitude != null) {
      return format("Lat: {1}, Lon: {0}", longitude, latitude);
    } else {
      return "geolocation: n/a";
    }
  }
}
