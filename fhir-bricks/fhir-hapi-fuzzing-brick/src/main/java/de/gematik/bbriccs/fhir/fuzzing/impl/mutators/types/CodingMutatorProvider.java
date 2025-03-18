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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;

@Getter
public class CodingMutatorProvider implements FhirTypeMutatorProvider<Coding> {

  private final List<FuzzingMutator<Coding>> mutators;

  public CodingMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Coding>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Coding>>();
    mutators.add((ctx, coding) -> ctx.fuzzIdElement(Coding.class, coding));

    mutators.add(
        (ctx, coding) ->
            ctx.fuzzChildTypes(
                Coding.class, ensureNotNull(ctx.randomness(), coding).getExtension()));

    mutators.add(
        (ctx, coding) ->
            ctx.fuzzChild(Coding.class, ensureNotNull(ctx.randomness(), coding).getCodeElement()));

    mutators.add(
        (ctx, coding) ->
            ctx.fuzzChild(
                Coding.class, ensureNotNull(ctx.randomness(), coding).getDisplayElement()));

    mutators.add(
        (ctx, coding) ->
            ctx.fuzzChild(
                Coding.class, ensureNotNull(ctx.randomness(), coding).getVersionElement()));

    mutators.add(
        (ctx, coding) ->
            ctx.fuzzChild(
                Coding.class, ensureNotNull(ctx.randomness(), coding).getUserSelectedElement()));

    mutators.add(
        (ctx, coding) -> {
          coding = ensureNotNull(ctx.randomness(), coding);
          val version = ctx.randomness().version();
          coding.setVersion(version);
          return FuzzingLogEntry.operation(
              format(
                  "Set random Version {0} to Coding {1} {2}",
                  version, coding.getSystem(), coding.getCode()));
        });

    return mutators;
  }

  private static Coding ensureNotNull(Randomness randomness, Coding coding) {
    if (coding == null) {
      coding = randomness.fhir().createType(Coding.class);
    }
    return coding;
  }
}
