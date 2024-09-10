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
import org.hl7.fhir.r4.model.UnsignedIntType;

@Getter
public class UnsignedIntTypeMutatorProvider implements FhirTypeMutatorProvider<UnsignedIntType> {

  private final List<FuzzingMutator<UnsignedIntType>> mutators;

  public UnsignedIntTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<UnsignedIntType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<UnsignedIntType>>();
    mutators.add((ctx, uit) -> ctx.fuzzIdElement(UnsignedIntType.class, uit));
    mutators.add(
        (ctx, uit) ->
            ctx.fuzzChildTypes(
                UnsignedIntType.class, ensureNotNull(ctx.randomness(), uit).getExtension()));

    mutators.add(
        (ctx, uit) -> {
          uit = ensureNotNull(ctx.randomness(), uit);
          val value = uit.getValue().intValue(); // NOSONAR unboxing required for logEntry
          val fValue = ctx.randomness().source().nextInt();
          uit.setValue(fValue);
          return FuzzingLogEntry.operation(
              format("Change Integer value: {0} -> {1}", value, fValue));
        });

    return mutators;
  }

  private static UnsignedIntType ensureNotNull(Randomness randomness, UnsignedIntType uit) {
    if (uit == null) {
      uit = randomness.fhir().createType(UnsignedIntType.class);
    }

    if (uit.getValue() == null) {
      uit.setValue(randomness.source().nextInt());
    }

    return uit;
  }
}
