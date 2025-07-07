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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Address;

@Getter
public class AddressTypeMutatorProvider implements FhirTypeMutatorProvider<Address> {

  private final List<FuzzingMutator<Address>> mutators;

  public AddressTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Address>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Address>>();

    mutators.add((ctx, address) -> ctx.fuzzIdElement(Address.class, address));

    mutators.add(
        (ctx, address) ->
            ctx.fuzzChildTypes(
                Address.class, ensureNotNull(ctx.randomness(), address).getExtension()));

    mutators.add(
        (ctx, address) ->
            ctx.fuzzChildTypes(Address.class, ensureNotNull(ctx.randomness(), address).getLine()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(
                Address.class, ensureNotNull(ctx.randomness(), address).getCityElement()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(
                Address.class, ensureNotNull(ctx.randomness(), address).getCountryElement()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(
                Address.class, ensureNotNull(ctx.randomness(), address).getDistrictElement()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(
                Address.class, ensureNotNull(ctx.randomness(), address).getPostalCodeElement()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(
                Address.class, ensureNotNull(ctx.randomness(), address).getStateElement()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(
                Address.class, ensureNotNull(ctx.randomness(), address).getTextElement()));
    mutators.add(
        (ctx, address) ->
            ctx.fuzzChild(Address.class, ensureNotNull(ctx.randomness(), address).getPeriod()));

    mutators.add(
        (ctx, address) -> {
          address = ensureNotNull(ctx.randomness(), address);
          val oc = address.getCountry();
          val fc = oc + ctx.randomness().regexify("[A-Z]{1,2}");
          address.setCountry(fc);
          return FuzzingLogEntry.operation(
              format("Change Address Country value: {0} -> {1}", oc, fc));
        });

    mutators.add(
        (ctx, address) -> {
          address = ensureNotNull(ctx.randomness(), address);
          val country = address.getCountry();
          val postalCode = address.getPostalCode();
          address.setCountry(postalCode);
          address.setPostalCode(country);
          return FuzzingLogEntry.operation(
              format(
                  "Flip Address Country and PostalCode values: {0} <-> {1}", country, postalCode));
        });

    mutators.add(
        (ctx, address) -> {
          address = ensureNotNull(ctx.randomness(), address);
          val oat = address.getType() == null ? Address.AddressType.NULL : address.getType();
          val fat = ctx.randomness().chooseRandomFromEnum(Address.AddressType.class, oat);
          address.setType(fat);
          return FuzzingLogEntry.operation(format("Change AddressType: {0} -> {1}", oat, fat));
        });

    return mutators;
  }

  private static Address ensureNotNull(Randomness randomness, Address address) {
    if (address == null) {
      address = randomness.fhir().createType(Address.class);
    }
    return address;
  }
}
