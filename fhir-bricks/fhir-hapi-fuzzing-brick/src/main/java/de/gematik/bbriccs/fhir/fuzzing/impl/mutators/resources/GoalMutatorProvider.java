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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Type;

public class GoalMutatorProvider extends BaseDomainResourceMutatorProvider<Goal> {

  public GoalMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Goal>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Goal>>();

    mutators.add(
        (ctx, goal) ->
            ctx.fuzzChildTypes(goal.getClass(), goal.getAddresses(), goal::getAddressesFirstRep));
    mutators.add(
        (ctx, goal) ->
            ctx.fuzzChildTypes(goal.getClass(), goal.getCategory(), goal::getCategoryFirstRep));
    mutators.add(
        (ctx, goal) ->
            ctx.fuzzChildTypes(goal.getClass(), goal.getIdentifier(), goal::getIdentifierFirstRep));
    mutators.add(
        (ctx, goal) -> ctx.fuzzChildTypes(goal.getClass(), goal.getNote(), goal::getNoteFirstRep));
    mutators.add(
        (ctx, goal) ->
            ctx.fuzzChildTypes(
                goal.getClass(), goal.getOutcomeCode(), goal::getOutcomeCodeFirstRep));

    mutators.add((ctx, goal) -> ctx.fuzzChild(goal, goal::hasSubject, goal::getSubject));
    mutators.add(
        (ctx, goal) -> ctx.fuzzChild(goal, goal::hasAchievementStatus, goal::getAchievementStatus));
    mutators.add((ctx, goal) -> ctx.fuzzChild(goal, goal::hasDescription, goal::getDescription));
    mutators.add((ctx, goal) -> ctx.fuzzChild(goal, goal::hasExpressedBy, goal::getExpressedBy));
    mutators.add((ctx, goal) -> ctx.fuzzChild(goal, goal::hasPriority, goal::getPriority));

    mutators.add(
        (ctx, goal) -> {
          if (goal.hasStart()) {
            return ctx.fuzzChild(goal, true, goal::getStart);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(goal::getStartDateType, goal::getStartCodeableConcept));
            return ctx.fuzzChild(goal.getClass(), goal::hasStart, supplier);
          }
        });

    mutators.add(
        (ctx, goal) -> {
          val status = goal.getLifecycleStatus();
          val fstatus =
              ctx.randomness().chooseRandomFromEnum(Goal.GoalLifecycleStatus.class, status);
          goal.setLifecycleStatus(fstatus);
          return FuzzingLogEntry.operation(
              format("Change LifeCycleStatus of {0}: {1} -> {2}", goal.getId(), status, fstatus));
        });

    return mutators;
  }
}
