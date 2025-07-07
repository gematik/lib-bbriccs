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
import org.hl7.fhir.r4.model.Narrative;

@Getter
public class NarrativeMutatorProvider implements FhirTypeMutatorProvider<Narrative> {

  private final List<FuzzingMutator<Narrative>> mutators;

  public NarrativeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Narrative>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Narrative>>();
    mutators.add((ctx, narrative) -> ctx.fuzzIdElement(Narrative.class, narrative));
    mutators.add(
        (ctx, narrative) ->
            ctx.fuzzChildTypes(
                Narrative.class, ensureNotNull(ctx.randomness(), narrative).getExtension()));

    mutators.add(
        (ctx, narrative) -> {
          narrative = ensureNotNull(ctx.randomness(), narrative);
          val ons = narrative.getStatus();
          val fns = ctx.randomness().chooseRandomFromEnum(Narrative.NarrativeStatus.class, ons);
          narrative.setStatus(fns);
          return FuzzingLogEntry.operation(
              format("Change Status of Narrative {0}: {1} -> {2}", narrative.getId(), ons, fns));
        });

    return mutators;
  }

  private static Narrative ensureNotNull(Randomness randomness, Narrative narrative) {
    if (narrative == null) {
      narrative = randomness.fhir().createType(Narrative.class);
    }

    if (!narrative.hasStatus()) {
      narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
    }
    return narrative;
  }
}
