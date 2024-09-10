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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.ProductShelfLife;

@Getter
public class ProductShelfLifeMutatorProvider implements FhirTypeMutatorProvider<ProductShelfLife> {

  private final List<FuzzingMutator<ProductShelfLife>> mutators;

  public ProductShelfLifeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<ProductShelfLife>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ProductShelfLife>>();
    mutators.add((ctx, psl) -> ctx.fuzzIdElement(ProductShelfLife.class, psl));
    mutators.add(
        (ctx, psl) ->
            ctx.fuzzChildTypes(
                ProductShelfLife.class, ensureNotNull(ctx.randomness(), psl).getExtension()));
    mutators.add(
        (ctx, psl) ->
            ctx.fuzzChildTypes(
                ProductShelfLife.class,
                ensureNotNull(ctx.randomness(), psl).getModifierExtension()));

    mutators.add(
        (ctx, psl) ->
            ctx.fuzzChildTypes(
                ProductShelfLife.class,
                ensureNotNull(ctx.randomness(), psl).getSpecialPrecautionsForStorage()));

    mutators.add(
        (ctx, psl) ->
            ctx.fuzzChild(ProductShelfLife.class, ensureNotNull(ctx.randomness(), psl).getType()));

    mutators.add(
        (ctx, psl) ->
            ctx.fuzzChild(
                ProductShelfLife.class, ensureNotNull(ctx.randomness(), psl).getIdentifier()));

    mutators.add(
        (ctx, psl) ->
            ctx.fuzzChild(
                ProductShelfLife.class, ensureNotNull(ctx.randomness(), psl).getPeriod()));

    return mutators;
  }

  private static ProductShelfLife ensureNotNull(Randomness randomness, ProductShelfLife psl) {
    if (psl == null) {
      psl = randomness.fhir().createType(ProductShelfLife.class);
    }

    return psl;
  }
}
