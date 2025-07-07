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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.MarketingStatus;

@Getter
public class MarketingStatusMutatorProvider implements FhirTypeMutatorProvider<MarketingStatus> {

  private final List<FuzzingMutator<MarketingStatus>> mutators;

  public MarketingStatusMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<MarketingStatus>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<MarketingStatus>>();
    mutators.add((ctx, ms) -> ctx.fuzzIdElement(MarketingStatus.class, ms));

    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChildTypes(
                MarketingStatus.class, ensureNotNull(ctx.randomness(), ms).getExtension()));
    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChildTypes(
                MarketingStatus.class, ensureNotNull(ctx.randomness(), ms).getModifierExtension()));
    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChild(MarketingStatus.class, ensureNotNull(ctx.randomness(), ms).getStatus()));
    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChild(MarketingStatus.class, ensureNotNull(ctx.randomness(), ms).getCountry()));
    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChild(
                MarketingStatus.class, ensureNotNull(ctx.randomness(), ms).getJurisdiction()));
    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChild(
                MarketingStatus.class, ensureNotNull(ctx.randomness(), ms).getDateRange()));
    mutators.add(
        (ctx, ms) ->
            ctx.fuzzChild(
                MarketingStatus.class,
                ensureNotNull(ctx.randomness(), ms).getRestoreDateElement()));

    return mutators;
  }

  private static MarketingStatus ensureNotNull(
      Randomness randomness, MarketingStatus marketingStatus) {
    if (marketingStatus == null) {
      marketingStatus = randomness.fhir().createType(MarketingStatus.class);
    }

    return marketingStatus;
  }
}
