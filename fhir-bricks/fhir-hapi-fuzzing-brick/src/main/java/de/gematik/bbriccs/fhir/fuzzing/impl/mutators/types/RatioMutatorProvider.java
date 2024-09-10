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
import org.hl7.fhir.r4.model.Ratio;

@Getter
public class RatioMutatorProvider implements FhirTypeMutatorProvider<Ratio> {

  private final List<FuzzingMutator<Ratio>> mutators;

  public RatioMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Ratio>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Ratio>>();
    mutators.add((ctx, ratio) -> ctx.fuzzIdElement(Ratio.class, ratio));
    mutators.add(
        (ctx, ratio) ->
            ctx.fuzzChildTypes(Ratio.class, ensureNotNull(ctx.randomness(), ratio).getExtension()));
    mutators.add(
        (ctx, ratio) ->
            ctx.fuzzChild(Ratio.class, ensureNotNull(ctx.randomness(), ratio).getDenominator()));
    mutators.add(
        (ctx, ratio) ->
            ctx.fuzzChild(Ratio.class, ensureNotNull(ctx.randomness(), ratio).getNumerator()));

    return mutators;
  }

  private static Ratio ensureNotNull(Randomness randomness, Ratio ratio) {
    if (ratio == null) {
      ratio = randomness.fhir().createType(Ratio.class);
    }
    return ratio;
  }
}
