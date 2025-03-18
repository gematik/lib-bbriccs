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
import org.hl7.fhir.r4.model.Claim;

public class ClaimMutatorProvider extends BaseDomainResourceMutatorProvider<Claim> {

  public ClaimMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Claim>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Claim>>();

    mutators.add(
        (ctx, claim) ->
            ctx.fuzzChildTypes(
                claim.getClass(), claim.getIdentifier(), claim::getIdentifierFirstRep));

    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasType, claim::getType));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasBillablePeriod, claim::getBillablePeriod));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasSubType, claim::getSubType));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasPriority, claim::getPriority));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasProvider, claim::getProvider));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasCreated, claim::getCreatedElement));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasEnterer, claim::getEnterer));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasFacility, claim::getFacility));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasFundsReserve, claim::getFundsReserve));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasInsurer, claim::getInsurer));
    mutators.add(
        (ctx, claim) ->
            ctx.fuzzChild(claim, claim::hasOriginalPrescription, claim::getOriginalPrescription));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasPatient, claim::getPatient));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasPrescription, claim::getPrescription));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasReferral, claim::getReferral));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasTotal, claim::getTotal));

    return mutators;
  }
}
