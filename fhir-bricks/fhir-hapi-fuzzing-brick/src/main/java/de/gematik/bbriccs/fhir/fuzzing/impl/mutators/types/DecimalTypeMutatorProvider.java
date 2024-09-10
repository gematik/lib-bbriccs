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
import org.hl7.fhir.r4.model.DecimalType;

@Getter
public class DecimalTypeMutatorProvider implements FhirTypeMutatorProvider<DecimalType> {

  private final List<FuzzingMutator<DecimalType>> mutators;

  public DecimalTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<DecimalType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<DecimalType>>();
    mutators.add((ctx, decimalType) -> ctx.fuzzIdElement(DecimalType.class, decimalType));

    mutators.add(
        (ctx, decimalType) ->
            ctx.fuzzChildTypes(
                DecimalType.class, ensureNotNull(ctx.randomness(), decimalType).getExtension()));

    mutators.add(
        (ctx, decimalType) -> {
          decimalType = ensureNotNull(ctx.randomness(), decimalType);
          val value = decimalType.getValue();
          val fValue = value.negate();
          decimalType.setValue(fValue);

          return FuzzingLogEntry.operation(
              format("Negate value of DecimalType {0}", decimalType.getId()));
        });

    return mutators;
  }

  private static DecimalType ensureNotNull(Randomness randomness, DecimalType decimalType) {
    if (decimalType == null) {
      decimalType = randomness.fhir().createType(DecimalType.class);
    }

    if (decimalType.getValue() == null) {
      decimalType.setValue(randomness.source().nextLong());
    }
    return decimalType;
  }
}
