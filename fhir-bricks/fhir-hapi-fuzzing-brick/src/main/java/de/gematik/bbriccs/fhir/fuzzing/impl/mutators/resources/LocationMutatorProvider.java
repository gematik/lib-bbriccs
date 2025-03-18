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
import org.hl7.fhir.r4.model.Location;

public class LocationMutatorProvider extends BaseDomainResourceMutatorProvider<Location> {

  public LocationMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Location>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Location>>();

    mutators.add(
        (ctx, location) ->
            ctx.fuzzChildTypes(
                location.getClass(), location.getEndpoint(), location::getEndpointFirstRep));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChildTypes(
                location.getClass(), location.getIdentifier(), location::getIdentifierFirstRep));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChildTypes(
                location.getClass(), location.getTelecom(), location::getTelecomFirstRep));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChildTypes(location.getClass(), location.getType(), location::getTypeFirstRep));

    mutators.add(
        (ctx, location) -> ctx.fuzzChild(location, location::hasAddress, location::getAddress));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChild(location, location::hasDescription, location::getDescriptionElement));
    mutators.add(
        (ctx, location) -> ctx.fuzzChild(location, location::hasName, location::getNameElement));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChild(
                location, location::hasManagingOrganization, location::getManagingOrganization));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChild(
                location, location::hasOperationalStatus, location::getOperationalStatus));
    mutators.add(
        (ctx, location) -> ctx.fuzzChild(location, location::hasPartOf, location::getPartOf));
    mutators.add(
        (ctx, location) ->
            ctx.fuzzChild(location, location::hasPhysicalType, location::getPhysicalType));

    return mutators;
  }
}
