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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;

@Getter
public class BooleanTypeMutatorProvider implements FhirTypeMutatorProvider<BooleanType> {

  private final List<FuzzingMutator<BooleanType>> mutators;

  public BooleanTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<BooleanType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<BooleanType>>();
    mutators.add((ctx, bt) -> ctx.fuzzIdElement(BooleanType.class, bt));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                BooleanType.class, ensureNotNull(ctx.randomness(), bt).getExtension()));

    mutators.add(
        (ctx, bt) -> {
          bt = ensureNotNull(ctx.randomness(), bt);
          val original = bt.booleanValue();
          bt.setValue(!original);
          return FuzzingLogEntry.operation("Flip BooleanType value");
        });

    return mutators;
  }

  private static BooleanType ensureNotNull(Randomness randomness, BooleanType booleanType) {
    if (booleanType == null) {
      booleanType = randomness.fhir().createType(BooleanType.class);
      booleanType.setValue(randomness.source().nextBoolean());
    }

    if (booleanType.getValue() == null) {
      booleanType.setValue(randomness.source().nextBoolean());
    }

    return booleanType;
  }
}
