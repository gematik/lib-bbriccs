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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Device;

public class DeviceMutatorProvider extends BaseDomainResourceMutatorProvider<Device> {

  public DeviceMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Device>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Device>>();

    mutators.add(
        (ctx, device) ->
            ctx.fuzzChildTypes(device.getClass(), device.getContact(), device::getContactFirstRep));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChildTypes(
                device.getClass(), device.getIdentifier(), device::getIdentifierFirstRep));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChildTypes(device.getClass(), device.getNote(), device::getNoteFirstRep));

    mutators.add(
        (ctx, device) -> ctx.fuzzChild(device, device::hasDefinition, device::getDefinition));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChild(
                device, device::hasDistinctIdentifier, device::getDistinctIdentifierElement));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChild(device, device::hasExpirationDate, device::getExpirationDateElement));
    mutators.add((ctx, device) -> ctx.fuzzChild(device, device::hasLocation, device::getLocation));
    mutators.add(
        (ctx, device) -> ctx.fuzzChild(device, device::hasLotNumber, device::getLotNumberElement));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChild(device, device::hasManufactureDate, device::getManufactureDateElement));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChild(device, device::hasManufacturer, device::getManufacturerElement));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChild(device, device::hasModelNumber, device::getModelNumberElement));
    mutators.add((ctx, device) -> ctx.fuzzChild(device, device::hasOwner, device::getOwner));
    mutators.add((ctx, device) -> ctx.fuzzChild(device, device::hasParent, device::getParent));
    mutators.add(
        (ctx, device) ->
            ctx.fuzzChild(device, device::hasPartNumber, device::getPartNumberElement));
    mutators.add((ctx, device) -> ctx.fuzzChild(device, device::hasPatient, device::getPatient));
    mutators.add((ctx, device) -> ctx.fuzzChild(device, device::hasType, device::getType));

    return mutators;
  }
}
