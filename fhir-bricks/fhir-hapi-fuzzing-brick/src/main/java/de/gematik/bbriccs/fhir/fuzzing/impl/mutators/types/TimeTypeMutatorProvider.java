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
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.TimeType;

@Getter
public class TimeTypeMutatorProvider implements FhirTypeMutatorProvider<TimeType> {

  private final List<FuzzingMutator<TimeType>> mutators;

  public TimeTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<TimeType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<TimeType>>();
    mutators.add((ctx, timeType) -> ctx.fuzzIdElement(TimeType.class, timeType));
    mutators.add(
        (ctx, timeType) ->
            ctx.fuzzChildTypes(
                TimeType.class, ensureNotNull(ctx.randomness(), timeType).getExtension()));
    mutators.add(
        (ctx, timeType) -> {
          timeType = ensureNotNull(ctx.randomness(), timeType);
          val time = ctx.randomness().time();
          val precision =
              ctx.randomness()
                  .chooseRandomFromEnum(
                      TemporalPrecisionEnum.class,
                      List.of(
                          TemporalPrecisionEnum.YEAR,
                          TemporalPrecisionEnum.MONTH,
                          TemporalPrecisionEnum.DAY));
          timeType.setValue(time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSS")));
          timeType.setPrecision(precision);
          return FuzzingLogEntry.operation(
              format("Change TimeType to {0}", timeType.getValueAsString()));
        });

    return mutators;
  }

  private static TimeType ensureNotNull(Randomness randomness, TimeType timeType) {
    if (timeType == null) {
      val precision =
          randomness.chooseRandomFromEnum(
              TemporalPrecisionEnum.class,
              List.of(
                  TemporalPrecisionEnum.YEAR,
                  TemporalPrecisionEnum.MONTH,
                  TemporalPrecisionEnum.DAY));
      timeType = randomness.fhir().createType(TimeType.class);
      timeType.setValue(randomness.time().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSS")));
      timeType.setPrecision(precision);
    }
    return timeType;
  }
}
