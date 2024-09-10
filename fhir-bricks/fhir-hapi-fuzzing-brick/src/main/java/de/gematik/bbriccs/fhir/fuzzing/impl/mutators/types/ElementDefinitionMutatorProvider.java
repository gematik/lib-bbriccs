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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.ElementDefinition;

@Getter
public class ElementDefinitionMutatorProvider
    implements FhirTypeMutatorProvider<ElementDefinition> {

  private final List<FuzzingMutator<ElementDefinition>> mutators;

  public ElementDefinitionMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<ElementDefinition>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ElementDefinition>>();
    mutators.add((ctx, ed) -> ctx.fuzzIdElement(ElementDefinition.class, ed));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChildTypes(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getExtension()));
    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChildTypes(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getCode()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class,
                ensureNotNull(ctx.randomness(), ed).getDefinitionElement()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getMaxValue()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getMaxElement()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getMinValue()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getMinElement()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getCommentElement()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class,
                ensureNotNull(ctx.randomness(), ed).getContentReferenceElement()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getDefaultValue()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getFixedOrPattern()));

    mutators.add(
        (ctx, ed) ->
            ctx.fuzzChild(
                ElementDefinition.class, ensureNotNull(ctx.randomness(), ed).getLabelElement()));

    return mutators;
  }

  private static ElementDefinition ensureNotNull(
      Randomness randomness, ElementDefinition elementDefinition) {
    if (elementDefinition == null) {
      elementDefinition = randomness.fhir().createType(ElementDefinition.class);
    }

    return elementDefinition;
  }
}
