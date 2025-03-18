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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Composition;

public class CompositionMutatorProvider extends BaseDomainResourceMutatorProvider<Composition> {

  public CompositionMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Composition>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Composition>>();

    mutators.add(
        (ctx, composition) ->
            ctx.fuzzChild(composition, composition::hasType, composition::getType));

    mutators.add(
        (ctx, composition) ->
            ctx.fuzzChildTypes(
                composition.getClass(), composition.getAuthor(), composition::getAuthorFirstRep));

    mutators.add(
        (ctx, composition) ->
            ctx.fuzzChildTypes(
                composition.getClass(),
                composition.getCategory(),
                composition::getCategoryFirstRep));

    mutators.add(
        (ctx, composition) ->
            ctx.randomness()
                .chooseRandomly(composition.getSection())
                .map(
                    sc -> {
                      composition.getSection().remove(sc);
                      return FuzzingLogEntry.operation(
                          format("Remove section for entry {0} from composition", sc));
                    })
                .orElseGet(
                    () -> FuzzingLogEntry.noop("do not remove any entries from composition")));

    mutators.add(
        (ctx, composition) -> {
          val section = new Composition.SectionComponent();
          val reference = section.addEntry().setReference(ctx.randomness().fhir().fhirResourceId());
          composition.getSection().add(section);
          val entry = ctx.fuzzChild(composition.getClass(), reference);
          return FuzzingLogEntry.add(
              format("Add random section to composition: {0}", reference.getReference()), entry);
        });

    return mutators;
  }
}
