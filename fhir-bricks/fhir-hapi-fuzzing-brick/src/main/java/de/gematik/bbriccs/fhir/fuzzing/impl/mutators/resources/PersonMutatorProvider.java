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
import org.hl7.fhir.r4.model.Person;

public class PersonMutatorProvider extends BaseDomainResourceMutatorProvider<Person> {

  public PersonMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Person>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Person>>();

    mutators.add(
        (ctx, person) ->
            ctx.fuzzChildTypes(person.getClass(), person.getAddress(), person::getAddressFirstRep));
    mutators.add(
        (ctx, person) ->
            ctx.fuzzChildTypes(
                person.getClass(), person.getIdentifier(), person::getIdentifierFirstRep));
    mutators.add(
        (ctx, person) ->
            ctx.fuzzChildTypes(person.getClass(), person.getName(), person::getNameFirstRep));
    mutators.add(
        (ctx, person) ->
            ctx.fuzzChildTypes(person.getClass(), person.getTelecom(), person::getTelecomFirstRep));

    mutators.add(
        (ctx, person) -> ctx.fuzzChild(person, person::hasActive, person::getActiveElement));
    mutators.add((ctx, person) -> ctx.fuzzChild(person, person::hasPhoto, person::getPhoto));
    mutators.add(
        (ctx, person) ->
            ctx.fuzzChild(
                person, person::hasManagingOrganization, person::getManagingOrganization));
    mutators.add(
        (ctx, person) -> ctx.fuzzChild(person, person::hasBirthDate, person::getBirthDateElement));

    return mutators;
  }
}
