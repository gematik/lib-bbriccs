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
import org.hl7.fhir.r4.model.CarePlan;

public class CarePlanMutatorProvider extends BaseDomainResourceMutatorProvider<CarePlan> {

  public CarePlanMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<CarePlan>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CarePlan>>();

    mutators.add(
        (cpx, cp) ->
            cpx.fuzzChildTypes(cp.getClass(), cp.getIdentifier(), cp::getIdentifierFirstRep));
    mutators.add(
        (cpx, cp) ->
            cpx.fuzzChildTypes(cp.getClass(), cp.getAddresses(), cp::getAddressesFirstRep));

    mutators.add(
        (cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getCategory(), cp::getCategoryFirstRep));
    mutators.add((cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getNote(), cp::getNoteFirstRep));
    mutators.add(
        (cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getCareTeam(), cp::getCareTeamFirstRep));
    mutators.add(
        (cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getBasedOn(), cp::getBasedOnFirstRep));
    mutators.add(
        (cpx, cp) ->
            cpx.fuzzChildTypes(cp.getClass(), cp.getContributor(), cp::getContributorFirstRep));
    mutators.add((cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getGoal(), cp::getGoalFirstRep));
    mutators.add(
        (cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getPartOf(), cp::getPartOfFirstRep));
    mutators.add(
        (cpx, cp) ->
            cpx.fuzzChildTypes(
                cp.getClass(), cp.getInstantiatesCanonical(), cp::addInstantiatesCanonicalElement));
    mutators.add(
        (cpx, cp) ->
            cpx.fuzzChildTypes(
                cp.getClass(), cp.getInstantiatesUri(), cp::addInstantiatesUriElement));
    mutators.add(
        (cpx, cp) -> cpx.fuzzChildTypes(cp.getClass(), cp.getReplaces(), cp::getReplacesFirstRep));
    mutators.add(
        (cpx, cp) ->
            cpx.fuzzChildTypes(
                cp.getClass(), cp.getSupportingInfo(), cp::getSupportingInfoFirstRep));

    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasAuthor, cp::getAuthor));
    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasSubject, cp::getSubject));
    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasEncounter, cp::getEncounter));
    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasPeriod, cp::getPeriod));
    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasCreated, cp::getCreatedElement));
    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasDescription, cp::getDescriptionElement));
    mutators.add((cpx, cp) -> cpx.fuzzChild(cp, cp::hasTitle, cp::getTitleElement));

    return mutators;
  }
}
