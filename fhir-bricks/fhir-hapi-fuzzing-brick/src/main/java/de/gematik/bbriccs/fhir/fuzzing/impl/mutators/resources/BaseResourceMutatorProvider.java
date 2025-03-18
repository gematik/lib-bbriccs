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

import de.gematik.bbriccs.fhir.fuzzing.FhirResourceMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Getter
public abstract class BaseResourceMutatorProvider<R extends Resource>
    implements FhirResourceMutatorProvider<R> {

  private final List<FuzzingMutator<R>> mutators;

  protected BaseResourceMutatorProvider(List<FuzzingMutator<R>> concreteMutators) {
    this.mutators = createMutators();
    this.mutators.addAll(0, concreteMutators);
  }

  private static <R extends Resource> List<FuzzingMutator<R>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<R>>();
    mutators.add((ctx, resource) -> ctx.fuzzIdElement(resource.getClass(), resource));
    mutators.add((ctx, resource) -> ctx.fuzzIdElement(resource.getClass(), resource));
    mutators.add((ctx, resource) -> ctx.fuzzChild(resource, resource.hasMeta(), resource::getMeta));
    mutators.add(
        (ctx, resource) ->
            ctx.fuzzChild(resource, resource.hasLanguageElement(), resource::getLanguageElement));
    mutators.add(
        (ctx, resource) ->
            ctx.fuzzChild(
                resource, resource.hasImplicitRulesElement(), resource::getImplicitRulesElement));
    return mutators;
  }
}
