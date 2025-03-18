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
 */

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.hl7.fhir.r4.model.Type;

public class SupplyRequestMutatorProvider extends BaseDomainResourceMutatorProvider<SupplyRequest> {

  public SupplyRequestMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<SupplyRequest>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<SupplyRequest>>();

    mutators.add(
        (ctx, sr) ->
            ctx.fuzzChildTypes(sr.getClass(), sr.getIdentifier(), sr::getIdentifierFirstRep));
    mutators.add(
        (ctx, sr) ->
            ctx.fuzzChildTypes(sr.getClass(), sr.getReasonCode(), sr::getReasonCodeFirstRep));
    mutators.add(
        (ctx, sr) -> ctx.fuzzChildTypes(sr.getClass(), sr.getSupplier(), sr::getSupplierFirstRep));

    mutators.add((ctx, sr) -> ctx.fuzzChild(sr, sr::hasAuthoredOn, sr::getAuthoredOnElement));
    mutators.add((ctx, sr) -> ctx.fuzzChild(sr, sr::hasRequester, sr::getRequester));

    mutators.add((ctx, sr) -> ctx.fuzzChild(sr, sr::hasCategory, sr::getCategory));
    mutators.add((ctx, sr) -> ctx.fuzzChild(sr, sr::hasDeliverFrom, sr::getDeliverFrom));
    mutators.add((ctx, sr) -> ctx.fuzzChild(sr, sr::hasDeliverTo, sr::getDeliverTo));
    mutators.add((ctx, sr) -> ctx.fuzzChild(sr, sr::hasQuantity, sr::getQuantity));

    mutators.add(
        (ctx, sr) -> {
          if (sr.hasOccurrence()) {
            return ctx.fuzzChild(sr, true, sr::getOccurrence);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(
                            sr::getOccurrencePeriod,
                            sr::getOccurrenceTiming,
                            sr::getOccurrenceDateTimeType));
            return ctx.fuzzChild(sr, false, supplier);
          }
        });

    mutators.add(
        (ctx, sr) -> {
          if (sr.hasItem()) {
            return ctx.fuzzChild(sr, true, sr::getItem);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(List.of(sr::getItemReference, sr::getItemCodeableConcept));
            return ctx.fuzzChild(sr, false, supplier);
          }
        });

    return mutators;
  }
}
