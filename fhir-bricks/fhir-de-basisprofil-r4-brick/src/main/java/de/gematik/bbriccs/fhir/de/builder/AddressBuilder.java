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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ElementBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Enumeration;

public class AddressBuilder extends ElementBuilder<Address, AddressBuilder> {

  //  won't be exposed to a user directly
  private static final Pattern STREET_PATTERN =
      Pattern.compile("^([\\D\\s]+)\\s*([\\d|\\w]+)?.*"); // NOSONAR

  private final AddressType addressType;
  private Country country = Country.D;
  private String city;
  private String postal;
  private String street;

  private AddressBuilder(AddressType addressType) {
    this.addressType = addressType;
  }

  public static AddressBuilder ofType(AddressType addressType) {
    return new AddressBuilder(addressType);
  }

  public static AddressBuilder ofPostalType() {
    return ofType(AddressType.POSTAL);
  }

  public static AddressBuilder ofPhysicalType() {
    return ofType(AddressType.PHYSICAL);
  }

  public static AddressBuilder ofBothTypes() {
    return ofType(AddressType.BOTH);
  }

  public AddressBuilder country(Country country) {
    this.country = country;
    return this;
  }

  public AddressBuilder city(String city) {
    this.city = city;
    return this;
  }

  public AddressBuilder postal(String postal) {
    this.postal = postal;
    return this;
  }

  public AddressBuilder street(String street) {
    this.street = street;
    return this;
  }

  @Override
  public Address build() {
    checkRequired();
    val address = new Address();
    val type = new Enumeration<>(new Address.AddressTypeEnumFactory(), addressType);
    address.setTypeElement(type);
    address.setCountry(country.getCode());
    address.setPostalCode(postal).setCity(city);

    val streetMatcher = STREET_PATTERN.matcher(street);
    if (!streetMatcher.matches()) {
      throw new BuilderException(format("Given Street {0} is invalid", street));
    }

    val streetName = streetMatcher.group(1).trim();
    val streetLine = address.addLineElement();
    streetLine.setValue(street);
    streetLine.addExtension(HL7StructDef.STREET_NAME.asStringExtension(streetName));

    // house number is optional, but if present, it should be added as a separate extension
    Optional.ofNullable(streetMatcher.group(2))
        .map(String::trim)
        .ifPresent(
            houseNumber ->
                streetLine.addExtension(HL7StructDef.HOUSE_NUMBER.asStringExtension(houseNumber)));

    return address;
  }

  private void checkRequired() {
    checkRequired(country, "AddressBuilder requires a Country");
    checkRequired(city, "AddressBuilder requires a City");
    checkRequired(postal, "AddressBuilder requires a Postal Code");
    checkRequired(street, "AddressBuilder requires a Street");
  }
}
