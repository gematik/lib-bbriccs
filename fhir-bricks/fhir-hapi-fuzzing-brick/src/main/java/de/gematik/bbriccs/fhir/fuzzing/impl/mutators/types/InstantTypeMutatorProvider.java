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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.InstantType;

@Getter
public class InstantTypeMutatorProvider implements FhirTypeMutatorProvider<InstantType> {

  private final List<FuzzingMutator<InstantType>> mutators;

  public InstantTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<InstantType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<InstantType>>();
    mutators.add((ctx, itt) -> ctx.fuzzIdElement(InstantType.class, itt));
    mutators.add(
        (ctx, itt) ->
            ctx.fuzzChildTypes(
                InstantType.class, ensureNotNull(ctx.randomness(), itt).getExtension()));

    mutators.add(
        (ctx, itt) -> {
          itt = ensureNotNull(ctx.randomness(), itt);
          val date = ctx.randomness().date();
          val precision = ctx.randomness().chooseRandomFromEnum(TemporalPrecisionEnum.class);
          itt.setValue(date, precision);
          return FuzzingLogEntry.operation(
              format("Change Date of InstantType to {0}", itt.getValueAsString()));
        });

    return mutators;
  }

  private static InstantType ensureNotNull(Randomness randomness, InstantType itt) {
    if (itt == null) {
      val precision = randomness.chooseRandomFromEnum(TemporalPrecisionEnum.class);
      itt = randomness.fhir().createType(InstantType.class);
      itt.setValue(randomness.date(), precision);
    }
    return itt;
  }
}
