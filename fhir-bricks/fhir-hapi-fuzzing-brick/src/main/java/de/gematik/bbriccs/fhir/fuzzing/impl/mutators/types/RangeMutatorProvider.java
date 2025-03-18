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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Range;

@Getter
public class RangeMutatorProvider implements FhirTypeMutatorProvider<Range> {

  private final List<FuzzingMutator<Range>> mutators;

  public RangeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Range>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Range>>();
    mutators.add((ctx, range) -> ctx.fuzzIdElement(Range.class, range));
    mutators.add(
        (ctx, range) ->
            ctx.fuzzChildTypes(Range.class, ensureNotNull(ctx.randomness(), range).getExtension()));
    mutators.add(
        (ctx, range) ->
            ctx.fuzzChild(Range.class, ensureNotNull(ctx.randomness(), range).getHigh()));
    mutators.add(
        (ctx, range) ->
            ctx.fuzzChild(Range.class, ensureNotNull(ctx.randomness(), range).getLow()));

    return mutators;
  }

  private static Range ensureNotNull(Randomness randomness, Range range) {
    if (range == null) {
      range = randomness.fhir().createType(Range.class);
    }
    return range;
  }
}
