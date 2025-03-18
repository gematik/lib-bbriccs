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
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Task;

@Getter
public class TaskMutatorProvider extends BaseDomainResourceMutatorProvider<Task> {

  public TaskMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Task>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Task>>();

    mutators.add(
        (ctx, task) ->
            ctx.fuzzChildTypes(task.getClass(), task.getIdentifier(), task::getIdentifierFirstRep));
    mutators.add(
        (ctx, task) -> ctx.fuzzChildTypes(task.getClass(), task.getNote(), task::getNoteFirstRep));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChildTypes(task.getClass(), task.getBasedOn(), task::getBasedOnFirstRep));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChildTypes(task.getClass(), task.getInsurance(), task::getInsuranceFirstRep));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChildTypes(task.getClass(), task.getPartOf(), task::getPartOfFirstRep));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChildTypes(
                task.getClass(), task.getPerformerType(), task::getPerformerTypeFirstRep));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChildTypes(
                task.getClass(), task.getRelevantHistory(), task::getRelevantHistoryFirstRep));

    mutators.add(
        (ctx, task) -> ctx.fuzzChild(task, task::hasAuthoredOn, task::getAuthoredOnElement));
    mutators.add(
        (ctx, task) -> ctx.fuzzChild(task, task::hasBusinessStatus, task::getBusinessStatus));
    mutators.add((ctx, task) -> ctx.fuzzChild(task, task::hasCode, task::getCode));
    mutators.add((ctx, task) -> ctx.fuzzChild(task, task::hasEncounter, task::getEncounter));
    mutators.add(
        (ctx, task) -> ctx.fuzzChild(task, task::hasDescription, task::getDescriptionElement));
    mutators.add((ctx, task) -> ctx.fuzzChild(task, task::hasReasonCode, task::getReasonCode));
    mutators.add((ctx, task) -> ctx.fuzzChild(task, task::hasRequester, task::getRequester));
    mutators.add(
        (ctx, task) -> ctx.fuzzChild(task, task::hasExecutionPeriod, task::getExecutionPeriod));
    mutators.add((ctx, task) -> ctx.fuzzChild(task, task::hasFocus, task::getFocus));
    mutators.add((ctx, task) -> ctx.fuzzChild(task, task::hasFor, task::getFor));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChild(
                task, task::hasInstantiatesCanonical, task::getInstantiatesCanonicalElement));
    mutators.add(
        (ctx, task) ->
            ctx.fuzzChild(task, task::hasInstantiatesUriElement, task::getInstantiatesUriElement));

    return mutators;
  }
}
