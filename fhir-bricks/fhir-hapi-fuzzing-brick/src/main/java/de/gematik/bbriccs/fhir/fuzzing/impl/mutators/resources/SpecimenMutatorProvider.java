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
import org.hl7.fhir.r4.model.Specimen;

public class SpecimenMutatorProvider extends BaseDomainResourceMutatorProvider<Specimen> {

  public SpecimenMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Specimen>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Specimen>>();

    mutators.add(
        (ctx, spec) ->
            ctx.fuzzChildTypes(spec.getClass(), spec.getCondition(), spec::getConditionFirstRep));
    mutators.add(
        (ctx, spec) ->
            ctx.fuzzChildTypes(spec.getClass(), spec.getIdentifier(), spec::getIdentifierFirstRep));
    mutators.add(
        (ctx, spec) -> ctx.fuzzChildTypes(spec.getClass(), spec.getNote(), spec::getNoteFirstRep));
    mutators.add(
        (ctx, spec) ->
            ctx.fuzzChildTypes(spec.getClass(), spec.getParent(), spec::getParentFirstRep));
    mutators.add(
        (ctx, spec) ->
            ctx.fuzzChildTypes(spec.getClass(), spec.getRequest(), spec::getRequestFirstRep));

    mutators.add((ctx, spec) -> ctx.fuzzChild(spec, spec::hasType, spec::getType));
    mutators.add(
        (ctx, spec) ->
            ctx.fuzzChild(spec, spec::hasAccessionIdentifier, spec::getAccessionIdentifier));
    mutators.add(
        (ctx, spec) -> ctx.fuzzChild(spec, spec::hasReceivedTime, spec::getReceivedTimeElement));
    mutators.add((ctx, spec) -> ctx.fuzzChild(spec, spec::hasSubject, spec::getSubject));

    return mutators;
  }
}
