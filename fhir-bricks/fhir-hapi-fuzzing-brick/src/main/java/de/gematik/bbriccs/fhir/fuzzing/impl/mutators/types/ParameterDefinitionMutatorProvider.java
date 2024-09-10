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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.ParameterDefinition;

@Getter
public class ParameterDefinitionMutatorProvider
    implements FhirTypeMutatorProvider<ParameterDefinition> {

  private final List<FuzzingMutator<ParameterDefinition>> mutators;

  public ParameterDefinitionMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<ParameterDefinition>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ParameterDefinition>>();
    mutators.add((ctx, pd) -> ctx.fuzzIdElement(ParameterDefinition.class, pd));
    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChildTypes(
                ParameterDefinition.class, ensureNotNull(ctx.randomness(), pd).getExtension()));

    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChild(
                ParameterDefinition.class, ensureNotNull(ctx.randomness(), pd).getTypeElement()));

    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChild(
                ParameterDefinition.class,
                ensureNotNull(ctx.randomness(), pd).getDocumentationElement()));

    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChild(
                ParameterDefinition.class, ensureNotNull(ctx.randomness(), pd).getMaxElement()));

    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChild(
                ParameterDefinition.class, ensureNotNull(ctx.randomness(), pd).getMinElement()));

    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChild(
                ParameterDefinition.class, ensureNotNull(ctx.randomness(), pd).getNameElement()));

    mutators.add(
        (ctx, pd) ->
            ctx.fuzzChild(
                ParameterDefinition.class,
                ensureNotNull(ctx.randomness(), pd).getProfileElement()));

    mutators.add(
        (ctx, pd) -> {
          pd = ensureNotNull(ctx.randomness(), pd);
          val use = pd.getUse();
          val fuse =
              ctx.randomness().chooseRandomFromEnum(ParameterDefinition.ParameterUse.class, use);
          pd.setUse(fuse);
          return FuzzingLogEntry.operation(
              format("Change Use of ParameterDefinition {0}: {1} -> {2}", pd.getId(), use, fuse));
        });

    return mutators;
  }

  private static ParameterDefinition ensureNotNull(Randomness randomness, ParameterDefinition pd) {
    if (pd == null) {
      pd = randomness.fhir().createType(ParameterDefinition.class);
    }

    return pd;
  }
}
