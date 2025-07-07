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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.SupplyDelivery;
import org.hl7.fhir.r4.model.Type;

public class SupplyDeliveryMutatorProvider
    extends BaseDomainResourceMutatorProvider<SupplyDelivery> {

  public SupplyDeliveryMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<SupplyDelivery>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<SupplyDelivery>>();

    mutators.add(
        (ctx, sd) ->
            ctx.fuzzChildTypes(sd.getClass(), sd.getIdentifier(), sd::getIdentifierFirstRep));
    mutators.add(
        (ctx, sd) -> ctx.fuzzChildTypes(sd.getClass(), sd.getBasedOn(), sd::getBasedOnFirstRep));
    mutators.add(
        (ctx, sd) -> ctx.fuzzChildTypes(sd.getClass(), sd.getPartOf(), sd::getPartOfFirstRep));
    mutators.add(
        (ctx, sd) -> ctx.fuzzChildTypes(sd.getClass(), sd.getReceiver(), sd::getReceiverFirstRep));

    mutators.add((ctx, sd) -> ctx.fuzzChild(sd, sd::hasSupplier, sd::getSupplier));

    mutators.add((ctx, sd) -> ctx.fuzzChild(sd, sd::hasType, sd::getType));
    mutators.add((ctx, sd) -> ctx.fuzzChild(sd, sd::hasDestination, sd::getDestination));
    mutators.add((ctx, sd) -> ctx.fuzzChild(sd, sd::hasPatient, sd::getPatient));

    mutators.add(
        (ctx, sd) -> {
          if (sd.hasOccurrence()) {
            return ctx.fuzzChild(sd, true, sd::getOccurrence);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            sd::getOccurrencePeriod,
                            sd::getOccurrenceTiming,
                            sd::getOccurrenceDateTimeType));
            return ctx.fuzzChild(sd, false, supplier);
          }
        });

    mutators.add(
        (ctx, sd) -> {
          val status = sd.getStatus();
          val fstatus =
              ctx.randomness()
                  .chooseRandomFromEnum(SupplyDelivery.SupplyDeliveryStatus.class, status);
          sd.setStatus(fstatus);
          return FuzzingLogEntry.operation(
              format(
                  "Change Status of SupplyDelivery {0}: {1} -> {2}", sd.getId(), status, fstatus));
        });

    return mutators;
  }
}
