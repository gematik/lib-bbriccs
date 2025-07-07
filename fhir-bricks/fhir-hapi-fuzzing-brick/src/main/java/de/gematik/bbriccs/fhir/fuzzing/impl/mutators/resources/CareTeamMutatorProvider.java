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
import org.hl7.fhir.r4.model.CareTeam;

public class CareTeamMutatorProvider extends BaseDomainResourceMutatorProvider<CareTeam> {

  public CareTeamMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<CareTeam>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CareTeam>>();

    mutators.add(
        (ctx, ct) ->
            ctx.fuzzChildTypes(ct.getClass(), ct.getIdentifier(), ct::getIdentifierFirstRep));
    mutators.add(
        (ctx, ct) -> ctx.fuzzChildTypes(ct.getClass(), ct.getCategory(), ct::getCategoryFirstRep));
    mutators.add(
        (ctx, ct) ->
            ctx.fuzzChildTypes(
                ct.getClass(), ct.getManagingOrganization(), ct::getManagingOrganizationFirstRep));
    mutators.add((ctx, ct) -> ctx.fuzzChildTypes(ct.getClass(), ct.getNote(), ct::getNoteFirstRep));
    mutators.add(
        (ctx, ct) ->
            ctx.fuzzChildTypes(ct.getClass(), ct.getReasonCode(), ct::getReasonCodeFirstRep));
    mutators.add(
        (ctx, ct) ->
            ctx.fuzzChildTypes(
                ct.getClass(), ct.getReasonReference(), ct::getReasonReferenceFirstRep));
    mutators.add(
        (ctx, ct) -> ctx.fuzzChildTypes(ct.getClass(), ct.getTelecom(), ct::getTelecomFirstRep));

    mutators.add((ctx, ct) -> ctx.fuzzChild(ct, ct::hasName, ct::getNameElement));
    mutators.add((ctx, ct) -> ctx.fuzzChild(ct, ct::hasSubject, ct::getSubject));
    mutators.add((ctx, ct) -> ctx.fuzzChild(ct, ct::hasEncounter, ct::getEncounter));
    mutators.add((ctx, ct) -> ctx.fuzzChild(ct, ct::hasPeriod, ct::getPeriod));

    return mutators;
  }
}
