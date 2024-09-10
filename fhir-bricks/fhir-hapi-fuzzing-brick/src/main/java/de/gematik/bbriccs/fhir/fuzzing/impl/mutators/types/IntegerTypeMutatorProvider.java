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
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.IntegerType;

@Getter
public class IntegerTypeMutatorProvider implements FhirTypeMutatorProvider<IntegerType> {

  private final List<FuzzingMutator<IntegerType>> mutators;

  public IntegerTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<IntegerType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<IntegerType>>();

    mutators.add((ctx, it) -> ctx.fuzzIdElement(IntegerType.class, it));
    mutators.add(
        (ctx, it) ->
            ctx.fuzzChildTypes(
                IntegerType.class, ensureNotNull(ctx.randomness(), it).getExtension()));

    mutators.add(
        (ctx, it) -> {
          it = ensureNotNull(ctx.randomness(), it);
          it.setValue(ctx.randomness().source().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
          return FuzzingLogEntry.operation("Fuzz IntegerType");
        });

    return mutators;
  }

  private static IntegerType ensureNotNull(Randomness randomness, IntegerType it) {
    if (it == null) {
      it = randomness.fhir().createType(IntegerType.class);
    }
    return it;
  }
}
