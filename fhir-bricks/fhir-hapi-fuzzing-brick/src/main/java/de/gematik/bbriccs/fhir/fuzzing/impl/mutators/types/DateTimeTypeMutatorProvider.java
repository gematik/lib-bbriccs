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

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;

@Getter
public class DateTimeTypeMutatorProvider implements FhirTypeMutatorProvider<DateTimeType> {

  private final List<FuzzingMutator<DateTimeType>> mutators;

  public DateTimeTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<DateTimeType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<DateTimeType>>();
    mutators.add((ctx, dtt) -> ctx.fuzzIdElement(DateTimeType.class, dtt));
    mutators.add(
        (ctx, dtt) ->
            ctx.fuzzChildTypes(
                DateTimeType.class, ensureNotNull(ctx.randomness(), dtt).getExtension()));
    mutators.add(
        (ctx, dtt) -> {
          dtt = ensureNotNull(ctx.randomness(), dtt);
          val date = ctx.randomness().date();
          val precision =
              ctx.randomness()
                  .chooseRandomFromEnum(TemporalPrecisionEnum.class, TemporalPrecisionEnum.MINUTE);
          dtt.setValue(date, precision);
          return FuzzingLogEntry.operation(
              format("Change Date of DateTimeType to {0}", dtt.getValueAsString()));
        });

    return mutators;
  }

  private static DateTimeType ensureNotNull(Randomness randomness, DateTimeType dtt) {
    if (dtt == null) {
      val precision =
          randomness.chooseRandomFromEnum(
              TemporalPrecisionEnum.class, TemporalPrecisionEnum.MINUTE);
      dtt = randomness.fhir().createType(DateTimeType.class);
      dtt.setValue(randomness.date(), precision);
    }
    return dtt;
  }
}
