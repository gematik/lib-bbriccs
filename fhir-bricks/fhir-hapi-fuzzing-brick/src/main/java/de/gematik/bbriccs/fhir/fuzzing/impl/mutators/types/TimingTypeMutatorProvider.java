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

import de.gematik.bbriccs.fhir.fuzzing.FhirType;
import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Timing;

@Getter
public class TimingTypeMutatorProvider implements FhirTypeMutatorProvider<Timing> {

  private final List<FuzzingMutator<Timing>> mutators;

  public TimingTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Timing>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Timing>>();
    mutators.add((ctx, timing) -> ctx.fuzzIdElement(Timing.class, timing));
    mutators.add(
        (ctx, timing) ->
            ctx.fuzzChildTypes(
                Timing.class, ensureNotNull(ctx.randomness(), timing).getExtension()));

    mutators.add(
        (ctx, timing) ->
            ctx.fuzzChildTypes(
                Timing.class, ensureNotNull(ctx.randomness(), timing).getModifierExtension()));

    mutators.add(
        (ctx, timing) ->
            ctx.fuzzChildTypes(Timing.class, ensureNotNull(ctx.randomness(), timing).getEvent()));

    mutators.add(
        (ctx, timing) ->
            ctx.fuzzChild(Timing.class, ensureNotNull(ctx.randomness(), timing).getCode()));

    mutators.add(
        (ctx, timing) -> {
          val repeatComponent = getRepeatComponent(ctx.randomness(), timing);
          if (!repeatComponent.hasBounds()) {
            val boundsType =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(FhirType.DURATION, FhirType.PERIOD, FhirType.RANGE));
            val bounds = ctx.randomness().fhir().createType(boundsType);
            repeatComponent.setBounds(bounds);
          }

          if (repeatComponent.hasBoundsDuration()) {
            return ctx.fuzzChild(repeatComponent.getClass(), repeatComponent.getBoundsDuration());
          } else if (repeatComponent.hasBoundsPeriod()) {
            return ctx.fuzzChild(repeatComponent.getClass(), repeatComponent.getBoundsPeriod());
          } else if (repeatComponent.hasBoundsRange()) {
            return ctx.fuzzChild(repeatComponent.getClass(), repeatComponent.getBoundsRange());
          } else {
            // should never haben, because we are setting bounds randomly!
            return FuzzingLogEntry.noop(
                "Repeated Component does not have any Duration/Period/Range to fuzz");
          }
        });

    mutators.add(
        (ctx, timing) -> {
          val repeatComponent = getRepeatComponent(ctx.randomness(), timing);
          return ctx.fuzzChild(repeatComponent.getClass(), repeatComponent.getCountElement());
        });

    mutators.add(
        (ctx, timing) -> {
          val repeatComponent = getRepeatComponent(ctx.randomness(), timing);
          return ctx.fuzzChild(repeatComponent.getClass(), repeatComponent.getCountMaxElement());
        });

    mutators.add(
        (ctx, timing) -> {
          val repeatComponent = getRepeatComponent(ctx.randomness(), timing);
          val unit = repeatComponent.getDurationUnit();
          val funit = ctx.randomness().chooseRandomFromEnum(Timing.UnitsOfTime.class, unit);
          repeatComponent.setDurationUnit(funit);
          return FuzzingLogEntry.operation(format("Change Duration Unit: {0} -> {1}", unit, funit));
        });

    mutators.add(
        (ctx, timing) -> {
          val repeatComponent = getRepeatComponent(ctx.randomness(), timing);
          val unit = repeatComponent.getPeriodUnit();
          val funit = ctx.randomness().chooseRandomFromEnum(Timing.UnitsOfTime.class, unit);
          repeatComponent.setPeriodUnit(funit);
          return FuzzingLogEntry.operation(format("Change Period Unit: {0} -> {1}", unit, funit));
        });

    return mutators;
  }

  private static Timing ensureNotNull(Randomness randomness, Timing timing) {
    if (timing == null) {
      timing = randomness.fhir().createType(Timing.class);
    }

    return timing;
  }

  private static Timing.TimingRepeatComponent getRepeatComponent(
      Randomness randomness, Timing timing) {
    timing = ensureNotNull(randomness, timing);
    if (!timing.hasRepeat()) {
      timing.setRepeat(new Timing.TimingRepeatComponent());
    }
    return timing.getRepeat();
  }
}
