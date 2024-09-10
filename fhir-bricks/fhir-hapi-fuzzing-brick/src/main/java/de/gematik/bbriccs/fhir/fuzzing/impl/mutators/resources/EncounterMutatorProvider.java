/*
 * Copyright 2024 gematik GmbH
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
import org.hl7.fhir.r4.model.Encounter;

public class EncounterMutatorProvider extends BaseDomainResourceMutatorProvider<Encounter> {

  public EncounterMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Encounter>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Encounter>>();

    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(), encounter.getIdentifier(), encounter::getIdentifierFirstRep));

    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(), encounter.getType(), encounter::getTypeFirstRep));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(), encounter.getAccount(), encounter::getAccountFirstRep));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(),
                encounter.getAppointment(),
                encounter::getAppointmentFirstRep));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(), encounter.getBasedOn(), encounter::getBasedOnFirstRep));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(),
                encounter.getEpisodeOfCare(),
                encounter::getEpisodeOfCareFirstRep));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(), encounter.getReasonCode(), encounter::getReasonCodeFirstRep));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChildTypes(
                encounter.getClass(),
                encounter.getReasonReference(),
                encounter::getReasonReferenceFirstRep));

    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChild(encounter, encounter::hasServiceType, encounter::getServiceType));
    mutators.add(
        (ctx, encounter) -> ctx.fuzzChild(encounter, encounter::hasLength, encounter::getLength));
    mutators.add(
        (ctx, encounter) -> ctx.fuzzChild(encounter, encounter::hasPartOf, encounter::getPartOf));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChild(encounter, encounter::hasPriority, encounter::getPriority));
    mutators.add(
        (ctx, encounter) ->
            ctx.fuzzChild(encounter, encounter::hasServiceProvider, encounter::getServiceProvider));
    mutators.add(
        (ctx, encounter) -> ctx.fuzzChild(encounter, encounter::hasSubject, encounter::getSubject));

    return mutators;
  }
}
