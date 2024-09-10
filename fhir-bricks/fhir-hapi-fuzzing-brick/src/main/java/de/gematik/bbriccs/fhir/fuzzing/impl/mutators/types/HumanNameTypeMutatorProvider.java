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
import org.hl7.fhir.r4.model.HumanName;

@Getter
public class HumanNameTypeMutatorProvider implements FhirTypeMutatorProvider<HumanName> {

  private final List<FuzzingMutator<HumanName>> mutators;

  public HumanNameTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<HumanName>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<HumanName>>();
    mutators.add((ctx, humanname) -> ctx.fuzzIdElement(HumanName.class, humanname));
    mutators.add(
        (ctx, humanname) ->
            ctx.fuzzChildTypes(
                HumanName.class, ensureNotNull(ctx.randomness(), humanname).getExtension()));
    mutators.add(
        (ctx, humanname) ->
            ctx.fuzzChild(
                HumanName.class, ensureNotNull(ctx.randomness(), humanname).getFamilyElement()));
    mutators.add(
        (ctx, humanname) ->
            ctx.fuzzChildTypes(
                HumanName.class, ensureNotNull(ctx.randomness(), humanname).getGiven()));
    mutators.add(
        (ctx, humanname) ->
            ctx.fuzzChildTypes(
                HumanName.class, ensureNotNull(ctx.randomness(), humanname).getPrefix()));
    mutators.add(
        (ctx, humanname) ->
            ctx.fuzzChildTypes(
                HumanName.class, ensureNotNull(ctx.randomness(), humanname).getSuffix()));
    mutators.add(
        (ctx, humanname) ->
            ctx.fuzzChild(HumanName.class, ensureNotNull(ctx.randomness(), humanname).getPeriod()));

    return mutators;
  }

  private static HumanName ensureNotNull(Randomness randomness, HumanName humanName) {
    if (humanName == null) {
      humanName = randomness.fhir().createType(HumanName.class);
    }
    return humanName;
  }
}
