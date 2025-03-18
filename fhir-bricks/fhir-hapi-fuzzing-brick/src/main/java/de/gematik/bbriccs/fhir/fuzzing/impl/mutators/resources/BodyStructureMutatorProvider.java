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
import org.hl7.fhir.r4.model.BodyStructure;

public class BodyStructureMutatorProvider extends BaseDomainResourceMutatorProvider<BodyStructure> {

  public BodyStructureMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<BodyStructure>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<BodyStructure>>();

    mutators.add(
        (ctx, bs) ->
            ctx.fuzzChildTypes(bs.getClass(), bs.getIdentifier(), bs::getIdentifierFirstRep));
    mutators.add(
        (ctx, bs) -> ctx.fuzzChildTypes(bs.getClass(), bs.getImage(), bs::getImageFirstRep));
    mutators.add(
        (ctx, bs) ->
            ctx.fuzzChildTypes(
                bs.getClass(), bs.getLocationQualifier(), bs::getLocationQualifierFirstRep));

    mutators.add((ctx, bs) -> ctx.fuzzChild(bs, bs::hasDescription, bs::getDescriptionElement));
    mutators.add((ctx, bs) -> ctx.fuzzChild(bs, bs::hasLocation, bs::getLocation));
    mutators.add((ctx, bs) -> ctx.fuzzChild(bs, bs::hasActive, bs::getActiveElement));
    mutators.add((ctx, bs) -> ctx.fuzzChild(bs, bs::hasMorphology, bs::getMorphology));
    mutators.add((ctx, bs) -> ctx.fuzzChild(bs, bs::hasPatient, bs::getPatient));

    return mutators;
  }
}
