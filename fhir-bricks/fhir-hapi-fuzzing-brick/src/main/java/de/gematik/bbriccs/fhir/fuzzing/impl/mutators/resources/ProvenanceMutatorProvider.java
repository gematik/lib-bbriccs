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
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Type;

public class ProvenanceMutatorProvider extends BaseDomainResourceMutatorProvider<Provenance> {

  public ProvenanceMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Provenance>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Provenance>>();

    mutators.add(
        (ctx, prov) ->
            ctx.fuzzChildTypes(prov.getClass(), prov.getPolicy(), prov::addPolicyElement));
    mutators.add(
        (ctx, prov) ->
            ctx.fuzzChildTypes(prov.getClass(), prov.getReason(), prov::getReasonFirstRep));
    mutators.add(
        (ctx, prov) ->
            ctx.fuzzChildTypes(prov.getClass(), prov.getSignature(), prov::getSignatureFirstRep));
    mutators.add(
        (ctx, prov) ->
            ctx.fuzzChildTypes(prov.getClass(), prov.getTarget(), prov::getTargetFirstRep));

    mutators.add((ctx, prov) -> ctx.fuzzChild(prov, prov::hasActivity, prov::getActivity));
    mutators.add((ctx, prov) -> ctx.fuzzChild(prov, prov::hasLocation, prov::getLocation));
    mutators.add((ctx, prov) -> ctx.fuzzChild(prov, prov::hasRecorded, prov::getRecordedElement));

    mutators.add(
        (ctx, prov) -> {
          if (prov.hasOccurred()) {
            return ctx.fuzzChild(prov, true, prov::getOccurred);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(prov::getOccurredDateTimeType, prov::getOccurredPeriod));
            return ctx.fuzzChild(prov, false, supplier);
          }
        });

    return mutators;
  }
}
