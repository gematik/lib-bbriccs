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
import org.hl7.fhir.r4.model.TriggerDefinition;

@Getter
public class TriggerDefinitionMutatorProvider
    implements FhirTypeMutatorProvider<TriggerDefinition> {

  private final List<FuzzingMutator<TriggerDefinition>> mutators;

  public TriggerDefinitionMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<TriggerDefinition>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<TriggerDefinition>>();
    mutators.add((ctx, td) -> ctx.fuzzIdElement(TriggerDefinition.class, td));
    mutators.add(
        (ctx, td) ->
            ctx.fuzzChildTypes(
                TriggerDefinition.class, ensureNotNull(ctx.randomness(), td).getExtension()));
    mutators.add(
        (ctx, td) ->
            ctx.fuzzChild(
                TriggerDefinition.class, ensureNotNull(ctx.randomness(), td).getCondition()));

    mutators.add(
        (ctx, td) ->
            ctx.fuzzChild(
                TriggerDefinition.class, ensureNotNull(ctx.randomness(), td).getNameElement()));

    mutators.add(
        (ctx, td) ->
            ctx.fuzzChild(
                TriggerDefinition.class, ensureNotNull(ctx.randomness(), td).getTiming()));

    mutators.add(
        (ctx, td) -> {
          td = ensureNotNull(ctx.randomness(), td);
          val type = td.getType();
          val ftype =
              ctx.randomness().chooseRandomFromEnum(TriggerDefinition.TriggerType.class, type);
          td.setType(ftype);
          return FuzzingLogEntry.operation(
              format("Change TriggerType of {0}: {1} -> {2}", td.getId(), type, ftype));
        });

    return mutators;
  }

  private static TriggerDefinition ensureNotNull(Randomness randomness, TriggerDefinition td) {
    if (td == null) {
      td = randomness.fhir().createType(TriggerDefinition.class);
    }

    return td;
  }
}
