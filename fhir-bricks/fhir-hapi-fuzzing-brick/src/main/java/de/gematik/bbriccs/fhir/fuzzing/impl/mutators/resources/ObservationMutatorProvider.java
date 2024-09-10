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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Type;

public class ObservationMutatorProvider extends BaseDomainResourceMutatorProvider<Observation> {

  public ObservationMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Observation>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Observation>>();

    mutators.add(
        (ctx, ob) ->
            ctx.fuzzChildTypes(ob.getClass(), ob.getIdentifier(), ob::getIdentifierFirstRep));
    mutators.add(
        (ctx, ob) -> ctx.fuzzChildTypes(ob.getClass(), ob.getBasedOn(), ob::getBasedOnFirstRep));
    mutators.add(
        (ctx, ob) -> ctx.fuzzChildTypes(ob.getClass(), ob.getCategory(), ob::getCategoryFirstRep));
    mutators.add(
        (ctx, ob) ->
            ctx.fuzzChildTypes(ob.getClass(), ob.getDerivedFrom(), ob::getDerivedFromFirstRep));
    mutators.add(
        (ctx, ob) -> ctx.fuzzChildTypes(ob.getClass(), ob.getFocus(), ob::getFocusFirstRep));
    mutators.add(
        (ctx, ob) ->
            ctx.fuzzChildTypes(ob.getClass(), ob.getHasMember(), ob::getHasMemberFirstRep));
    mutators.add(
        (ctx, ob) ->
            ctx.fuzzChildTypes(
                ob.getClass(), ob.getInterpretation(), ob::getInterpretationFirstRep));
    mutators.add((ctx, ob) -> ctx.fuzzChildTypes(ob.getClass(), ob.getNote(), ob::getNoteFirstRep));
    mutators.add(
        (ctx, ob) -> ctx.fuzzChildTypes(ob.getClass(), ob.getPartOf(), ob::getPartOfFirstRep));
    mutators.add(
        (ctx, ob) ->
            ctx.fuzzChildTypes(ob.getClass(), ob.getPerformer(), ob::getPerformerFirstRep));

    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasBodySite, ob::getBodySite));
    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasCode, ob::getCode));
    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasDataAbsentReason, ob::getDataAbsentReason));
    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasDevice, ob::getDevice));
    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasEncounter, ob::getEncounter));
    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasMethod, ob::getMethod));
    mutators.add((ctx, ob) -> ctx.fuzzChild(ob, ob::hasSpecimen, ob::getSpecimen));

    mutators.add(
        (ctx, ob) -> {
          if (ob.hasValue()) {
            return ctx.fuzzChild(ob, true, ob::getValue);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            ob::getValueBooleanType,
                            ob::getValueRange,
                            ob::getValueIntegerType,
                            ob::getValueTimeType,
                            ob::getValueStringType,
                            ob::getValueDateTimeType,
                            ob::getValueCodeableConcept,
                            ob::getValuePeriod,
                            ob::getValueQuantity,
                            ob::getValueRatio,
                            ob::getValueSampledData));
            return ctx.fuzzChild(ob, false, supplier);
          }
        });

    mutators.add(
        (ctx, ob) -> {
          if (ob.hasEffective()) {
            return ctx.fuzzChild(ob, true, ob::getEffective);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            ob::getEffectiveInstantType,
                            ob::getEffectivePeriod,
                            ob::getEffectiveTiming,
                            ob::getEffectiveDateTimeType));
            return ctx.fuzzChild(ob, false, supplier);
          }
        });

    return mutators;
  }
}
