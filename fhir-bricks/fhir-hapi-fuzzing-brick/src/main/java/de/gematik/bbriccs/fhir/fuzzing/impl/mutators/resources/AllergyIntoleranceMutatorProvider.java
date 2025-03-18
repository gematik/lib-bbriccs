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
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Type;

public class AllergyIntoleranceMutatorProvider
    extends BaseDomainResourceMutatorProvider<AllergyIntolerance> {

  public AllergyIntoleranceMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<AllergyIntolerance>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<AllergyIntolerance>>();

    mutators.add(
        (ctx, ai) ->
            ctx.fuzzChildTypes(ai.getClass(), ai.getIdentifier(), ai::getIdentifierFirstRep));
    mutators.add((ctx, ai) -> ctx.fuzzChildTypes(ai.getClass(), ai.getNote(), ai::getNoteFirstRep));

    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasEncounter, ai::getEncounter));
    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasAsserter, ai::getAsserter));
    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasClinicalStatus, ai::getClinicalStatus));
    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasCode, ai::getCode));
    mutators.add(
        (ctx, ai) -> ctx.fuzzChild(ai, ai::hasLastOccurrence, ai::getLastOccurrenceElement));
    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasPatient, ai::getPatient));
    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasRecordedDate, ai::getRecordedDateElement));
    mutators.add((ctx, ai) -> ctx.fuzzChild(ai, ai::hasRecorder, ai::getRecorder));
    mutators.add(
        (ctx, ai) -> ctx.fuzzChild(ai, ai::hasVerificationStatus, ai::getVerificationStatus));

    mutators.add(
        (ctx, ai) -> {
          if (ai.hasOnset()) {
            return ctx.fuzzChild(ai, true, ai::getOnset);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            ai::getOnsetAge,
                            ai::getOnsetPeriod,
                            ai::getOnsetRange,
                            ai::getOnsetDateTimeType,
                            ai::getOnsetStringType));
            return ctx.fuzzChild(ai, ai::hasOnset, supplier);
          }
        });

    return mutators;
  }
}
