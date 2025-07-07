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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;

@Getter
public class QuantityMutatorProvider implements FhirTypeMutatorProvider<Quantity> {

  private final List<FuzzingMutator<Quantity>> mutators;

  public QuantityMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Quantity>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Quantity>>();
    mutators.add((ctx, quantity) -> ctx.fuzzIdElement(Quantity.class, quantity));
    mutators.add(
        (ctx, quantity) ->
            ctx.fuzzChildTypes(
                Quantity.class, ensureNotNull(ctx.randomness(), quantity).getExtension()));

    mutators.add(
        (ctx, quantity) ->
            ctx.fuzzChild(
                Quantity.class, ensureNotNull(ctx.randomness(), quantity).getCodeElement()));

    mutators.add(
        (ctx, quantity) ->
            ctx.fuzzChild(
                Quantity.class, ensureNotNull(ctx.randomness(), quantity).getSystemElement()));
    mutators.add(
        (ctx, quantity) ->
            ctx.fuzzChild(
                Quantity.class, ensureNotNull(ctx.randomness(), quantity).getUnitElement()));

    mutators.add(
        (ctx, quantity) ->
            ctx.fuzzChild(
                Quantity.class, ensureNotNull(ctx.randomness(), quantity).getValueElement()));

    mutators.add(
        (ctx, quantity) -> {
          quantity = ensureNotNull(ctx.randomness(), quantity);
          val oqce = quantity.getComparatorElement();
          val oqc = oqce.getValue();
          val fqc = ctx.randomness().chooseRandomFromEnum(Quantity.QuantityComparator.class, oqc);
          oqce.setValue(fqc);
          return FuzzingLogEntry.operation(
              format("Change QuantityComparator: {0} -> {1}", oqc, fqc));
        });

    return mutators;
  }

  private static Quantity ensureNotNull(Randomness randomness, Quantity period) {
    if (period == null) {
      period = randomness.fhir().createType(Quantity.class);
    }
    return period;
  }
}
