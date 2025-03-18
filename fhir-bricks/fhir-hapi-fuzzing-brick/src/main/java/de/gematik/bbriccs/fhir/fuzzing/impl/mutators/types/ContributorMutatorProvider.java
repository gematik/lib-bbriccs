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
import org.hl7.fhir.r4.model.Contributor;

@Getter
public class ContributorMutatorProvider implements FhirTypeMutatorProvider<Contributor> {

  private final List<FuzzingMutator<Contributor>> mutators;

  public ContributorMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Contributor>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Contributor>>();
    mutators.add((ctx, contributor) -> ctx.fuzzIdElement(Contributor.class, contributor));

    mutators.add(
        (ctx, contributor) ->
            ctx.fuzzChildTypes(
                Contributor.class, ensureNotNull(ctx.randomness(), contributor).getExtension()));

    mutators.add(
        (ctx, contributor) ->
            ctx.fuzzChild(
                Contributor.class, ensureNotNull(ctx.randomness(), contributor).getNameElement()));

    mutators.add(
        (ctx, contributor) ->
            ctx.fuzzChildTypes(
                Contributor.class, ensureNotNull(ctx.randomness(), contributor).getContact()));

    mutators.add(
        (ctx, contributor) -> {
          contributor = ensureNotNull(ctx.randomness(), contributor);
          val type = contributor.getType();
          val ftype =
              ctx.randomness().chooseRandomFromEnum(Contributor.ContributorType.class, type);
          contributor.setType(ftype);
          return FuzzingLogEntry.operation(
              format(
                  "Change Type of Contributor {0}: {1} -> {2}", contributor.getId(), type, ftype));
        });

    return mutators;
  }

  private static Contributor ensureNotNull(Randomness randomness, Contributor contributor) {
    if (contributor == null) {
      contributor = randomness.fhir().createType(Contributor.class);
    }

    return contributor;
  }
}
