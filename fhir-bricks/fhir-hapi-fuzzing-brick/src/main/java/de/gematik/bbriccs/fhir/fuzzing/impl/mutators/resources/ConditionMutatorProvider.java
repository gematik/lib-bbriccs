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
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Type;

@Getter
public class ConditionMutatorProvider extends BaseDomainResourceMutatorProvider<Condition> {

  public ConditionMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Condition>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Condition>>();

    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChildTypes(
                condition.getClass(), condition.getCategory(), condition::getCategoryFirstRep));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChildTypes(
                condition.getClass(), condition.getBodySite(), condition::getBodySiteFirstRep));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChildTypes(
                condition.getClass(), condition.getIdentifier(), condition::getIdentifierFirstRep));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChildTypes(
                condition.getClass(), condition.getNote(), condition::getNoteFirstRep));

    mutators.add(
        (ctx, condition) -> ctx.fuzzChild(condition, condition::hasSubject, condition::getSubject));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(condition, condition::hasClinicalStatus, condition::getClinicalStatus));
    mutators.add(
        (ctx, condition) -> ctx.fuzzChild(condition, condition::hasCode, condition::getCode));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(condition, condition::hasEncounter, condition::getEncounter));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(
                condition, condition::hasRecordedDate, condition::getRecordedDateElement));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(condition, condition::hasRecorder, condition::getRecorder));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(condition, condition::hasSeverity, condition::getSeverity));
    mutators.add(
        (ctx, condition) -> ctx.fuzzChild(condition, condition::hasSubject, condition::getSubject));
    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(
                condition, condition::hasVerificationStatus, condition::getVerificationStatus));

    mutators.add(
        (ctx, condition) ->
            ctx.fuzzChild(condition, condition::hasAsserter, condition::getAsserter));

    mutators.add(
        (ctx, condition) -> {
          if (condition.hasAbatement()) {
            return ctx.fuzzChild(condition, true, condition::getAbatement);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            condition::getAbatementAge,
                            condition::getAbatementPeriod,
                            condition::getAbatementRange,
                            condition::getAbatementDateTimeType,
                            condition::getAbatementStringType));
            return ctx.fuzzChild(condition, false, supplier);
          }
        });

    mutators.add(
        (ctx, condition) -> {
          if (condition.hasOnset()) {
            return ctx.fuzzChild(condition, true, condition::getOnset);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            condition::getOnsetAge,
                            condition::getOnsetPeriod,
                            condition::getOnsetRange,
                            condition::getOnsetDateTimeType,
                            condition::getOnsetStringType));
            return ctx.fuzzChild(condition, false, supplier);
          }
        });

    return mutators;
  }
}
