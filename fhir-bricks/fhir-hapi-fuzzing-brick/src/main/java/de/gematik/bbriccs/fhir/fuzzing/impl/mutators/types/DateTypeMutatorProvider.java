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
import org.hl7.fhir.r4.model.DateType;

@Getter
public class DateTypeMutatorProvider implements FhirTypeMutatorProvider<DateType> {

  private final List<FuzzingMutator<DateType>> mutators;

  public DateTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<DateType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<DateType>>();
    mutators.add((ctx, dt) -> ctx.fuzzIdElement(DateType.class, dt));
    mutators.add(
        (ctx, dt) ->
            ctx.fuzzChildTypes(DateType.class, ensureNotNull(ctx.randomness(), dt).getExtension()));
    mutators.add(
        (ctx, dt) -> {
          dt = ensureNotNull(ctx.randomness(), dt);
          val date = ctx.randomness().date();
          val precision =
              ctx.randomness()
                  .chooseRandomFromEnum(
                      TemporalPrecisionEnum.class,
                      List.of(
                          TemporalPrecisionEnum.SECOND,
                          TemporalPrecisionEnum.MILLI,
                          TemporalPrecisionEnum.MINUTE));
          dt.setValue(date, precision);
          return FuzzingLogEntry.operation(
              format("Change Date of DateType to {0}", dt.getValueAsString()));
        });

    return mutators;
  }

  private static DateType ensureNotNull(Randomness randomness, DateType dt) {
    if (dt == null) {
      val precision =
          randomness.chooseRandomFromEnum(
              TemporalPrecisionEnum.class,
              List.of(
                  TemporalPrecisionEnum.SECOND,
                  TemporalPrecisionEnum.MILLI,
                  TemporalPrecisionEnum.MINUTE));
      dt = randomness.fhir().createType(DateType.class);
      dt.setValue(randomness.date(), precision);
    }
    return dt;
  }
}
