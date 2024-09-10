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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.SubstanceAmount;

@Getter
public class SubstanceAmountMutatorProvider implements FhirTypeMutatorProvider<SubstanceAmount> {

  private final List<FuzzingMutator<SubstanceAmount>> mutators;

  public SubstanceAmountMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<SubstanceAmount>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<SubstanceAmount>>();
    mutators.add((ctx, sa) -> ctx.fuzzIdElement(SubstanceAmount.class, sa));
    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChildTypes(
                SubstanceAmount.class, ensureNotNull(ctx.randomness(), sa).getExtension()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChildTypes(
                SubstanceAmount.class, ensureNotNull(ctx.randomness(), sa).getModifierExtension()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SubstanceAmount.class, ensureNotNull(ctx.randomness(), sa).getAmountType()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(SubstanceAmount.class, ensureNotNull(ctx.randomness(), sa).getAmount()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SubstanceAmount.class, ensureNotNull(ctx.randomness(), sa).getAmountTextElement()));

    mutators.add(
        (ctx, sa) -> {
          sa = ensureNotNull(ctx.randomness(), sa);
          val refRange = sa.getReferenceRange();
          val lowLimit = ctx.fuzzChild(sa.getClass(), refRange.getLowLimit());
          val highLimit = ctx.fuzzChild(sa.getClass(), refRange.getHighLimit());
          return FuzzingLogEntry.parent(
              format("Fuzz Low/High Limits of SubstanceAmount {0}", sa.getId()),
              List.of(lowLimit, highLimit));
        });

    return mutators;
  }

  private static SubstanceAmount ensureNotNull(Randomness randomness, SubstanceAmount sa) {
    if (sa == null) {
      sa = randomness.fhir().createType(SubstanceAmount.class);
    }

    return sa;
  }
}
