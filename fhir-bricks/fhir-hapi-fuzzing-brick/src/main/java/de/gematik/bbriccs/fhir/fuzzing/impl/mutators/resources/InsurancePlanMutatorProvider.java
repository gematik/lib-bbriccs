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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.InsurancePlan;

public class InsurancePlanMutatorProvider extends BaseDomainResourceMutatorProvider<InsurancePlan> {

  public InsurancePlanMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<InsurancePlan>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<InsurancePlan>>();

    mutators.add(
        (ctx, ip) ->
            ctx.fuzzChildTypes(ip.getClass(), ip.getIdentifier(), ip::getIdentifierFirstRep));
    mutators.add((ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getType(), ip::getTypeFirstRep));
    mutators.add(
        (ctx, ip) ->
            ctx.fuzzChildTypes(ip.getClass(), ip.getCoverageArea(), ip::getCoverageAreaFirstRep));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getEndpoint(), ip::getEndpointFirstRep));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getNetwork(), ip::getNetworkFirstRep));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getAlias(), ip::addAliasElement));

    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasAdministeredBy, ip::getAdministeredBy));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasName, ip::getNameElement));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasOwnedBy, ip::getOwnedBy));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasPeriod, ip::getPeriod));

    return mutators;
  }
}
