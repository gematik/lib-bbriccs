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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.PositiveIntType;

@Getter
public class PositiveIntTypeMutatorProvider implements FhirTypeMutatorProvider<PositiveIntType> {

  private final List<FuzzingMutator<PositiveIntType>> mutators;

  public PositiveIntTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<PositiveIntType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<PositiveIntType>>();
    mutators.add((ctx, pit) -> ctx.fuzzIdElement(PositiveIntType.class, pit));
    mutators.add(
        (ctx, pit) ->
            ctx.fuzzChildTypes(
                PositiveIntType.class, ensureNotNull(ctx.randomness(), pit).getExtension()));

    mutators.add(
        (ctx, pit) -> {
          pit = ensureNotNull(ctx.randomness(), pit);
          pit.setValue(ctx.randomness().source().nextInt(0, Integer.MAX_VALUE));
          return FuzzingLogEntry.operation("Fuzz PositiveIntType");
        });

    return mutators;
  }

  private static PositiveIntType ensureNotNull(Randomness randomness, PositiveIntType pit) {
    if (pit == null) {
      pit = randomness.fhir().createType(PositiveIntType.class);
    }
    return pit;
  }
}
