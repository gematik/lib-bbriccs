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
import org.hl7.fhir.r4.model.SampledData;

@Getter
public class SampleDataMutatorProvider implements FhirTypeMutatorProvider<SampledData> {

  private final List<FuzzingMutator<SampledData>> mutators;

  public SampleDataMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<SampledData>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<SampledData>>();
    mutators.add((ctx, sa) -> ctx.fuzzIdElement(SampledData.class, sa));
    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChildTypes(
                SampledData.class, ensureNotNull(ctx.randomness(), sa).getExtension()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(SampledData.class, ensureNotNull(ctx.randomness(), sa).getDataElement()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SampledData.class, ensureNotNull(ctx.randomness(), sa).getDimensionsElement()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SampledData.class, ensureNotNull(ctx.randomness(), sa).getFactorElement()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SampledData.class, ensureNotNull(ctx.randomness(), sa).getLowerLimitElement()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SampledData.class, ensureNotNull(ctx.randomness(), sa).getUpperLimitElement()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(SampledData.class, ensureNotNull(ctx.randomness(), sa).getOrigin()));

    mutators.add(
        (ctx, sa) ->
            ctx.fuzzChild(
                SampledData.class, ensureNotNull(ctx.randomness(), sa).getPeriodElement()));

    return mutators;
  }

  private static SampledData ensureNotNull(Randomness randomness, SampledData sa) {
    if (sa == null) {
      sa = randomness.fhir().createType(SampledData.class);
    }

    return sa;
  }
}
