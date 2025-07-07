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
import org.hl7.fhir.r4.model.ClaimResponse;

public class ClaimResponseMutatorProvider extends BaseDomainResourceMutatorProvider<ClaimResponse> {

  public ClaimResponseMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<ClaimResponse>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ClaimResponse>>();

    mutators.add(
        (ctx, claim) ->
            ctx.fuzzChildTypes(
                claim.getClass(), claim.getIdentifier(), claim::getIdentifierFirstRep));
    mutators.add(
        (ctx, claim) ->
            ctx.fuzzChildTypes(
                claim.getClass(),
                claim.getCommunicationRequest(),
                claim::getCommunicationRequestFirstRep));

    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasType, claim::getType));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasSubType, claim::getSubType));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasCreated, claim::getCreatedElement));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasFundsReserve, claim::getFundsReserve));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasInsurer, claim::getInsurer));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasPatient, claim::getPatient));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasPayeeType, claim::getPayeeType));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasDisposition, claim::getDispositionElement));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasForm, claim::getForm));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasFormCode, claim::getFormCode));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasPreAuthPeriod, claim::getPreAuthPeriod));
    mutators.add(
        (ctx, claim) -> ctx.fuzzChild(claim, claim::hasPreAuthRef, claim::getPreAuthRefElement));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasRequest, claim::getRequest));
    mutators.add((ctx, claim) -> ctx.fuzzChild(claim, claim::hasRequestor, claim::getRequestor));

    return mutators;
  }
}
