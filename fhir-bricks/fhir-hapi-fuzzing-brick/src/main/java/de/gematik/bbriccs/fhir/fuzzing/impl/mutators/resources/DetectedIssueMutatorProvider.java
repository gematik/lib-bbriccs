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
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.DetectedIssue;
import org.hl7.fhir.r4.model.Type;

public class DetectedIssueMutatorProvider extends BaseDomainResourceMutatorProvider<DetectedIssue> {

  public DetectedIssueMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<DetectedIssue>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<DetectedIssue>>();

    mutators.add(
        (ctx, di) ->
            ctx.fuzzChildTypes(di.getClass(), di.getIdentifier(), di::getIdentifierFirstRep));
    mutators.add(
        (ctx, di) ->
            ctx.fuzzChildTypes(di.getClass(), di.getImplicated(), di::getImplicatedFirstRep));

    mutators.add((ctx, di) -> ctx.fuzzChild(di, di::hasAuthor, di::getAuthor));
    mutators.add((ctx, di) -> ctx.fuzzChild(di, di::hasCode, di::getCode));
    mutators.add((ctx, di) -> ctx.fuzzChild(di, di::hasDetail, di::getDetailElement));
    mutators.add((ctx, di) -> ctx.fuzzChild(di, di::hasPatient, di::getPatient));
    mutators.add((ctx, di) -> ctx.fuzzChild(di, di::hasReference, di::getReferenceElement));

    mutators.add(
        (ctx, di) -> {
          if (di.hasIdentified()) {
            return ctx.fuzzChild(di, true, di::getIdentified);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(di::getIdentifiedPeriod, di::getIdentifiedDateTimeType));
            return ctx.fuzzChild(di, false, supplier);
          }
        });

    return mutators;
  }
}
