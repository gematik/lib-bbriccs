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
import org.hl7.fhir.r4.model.Organization;

public class OrganizationMutatorProvider extends BaseDomainResourceMutatorProvider<Organization> {

  public OrganizationMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Organization>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Organization>>();

    mutators.add(
        (ctx, org) ->
            ctx.fuzzChildTypes(org.getClass(), org.getIdentifier(), org::getIdentifierFirstRep));

    mutators.add(
        (ctx, org) ->
            ctx.fuzzChildTypes(org.getClass(), org.getEndpoint(), org::getEndpointFirstRep));

    mutators.add(
        (ctx, org) ->
            ctx.fuzzChildTypes(org.getClass(), org.getAddress(), org::getAddressFirstRep));

    mutators.add(
        (ctx, org) -> ctx.fuzzChildTypes(org.getClass(), org.getType(), org::getTypeFirstRep));

    return mutators;
  }
}
