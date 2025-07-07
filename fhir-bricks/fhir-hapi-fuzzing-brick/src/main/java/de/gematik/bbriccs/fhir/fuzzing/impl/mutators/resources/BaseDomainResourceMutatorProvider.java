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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.DomainResource;

public abstract class BaseDomainResourceMutatorProvider<R extends DomainResource>
    extends BaseResourceMutatorProvider<R> {

  protected BaseDomainResourceMutatorProvider(List<FuzzingMutator<R>> concreteMutators) {
    super(createMutators());
    this.getMutators().addAll(0, concreteMutators);
  }

  private static <R extends DomainResource> List<FuzzingMutator<R>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<R>>();

    mutators.add(
        (ctx, dr) -> ctx.fuzzChildTypes(dr.getClass(), dr.getExtension(), dr::addExtension));
    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChildTypes(dr.getClass(), dr.getModifierExtension(), dr::addModifierExtension));

    mutators.add((ctx, dr) -> ctx.fuzzChild(dr, dr.hasText(), dr::getText));

    mutators.add((ctx, dr) -> ctx.fuzzChildResources(dr.getClass(), dr.getContained()));
    mutators.add(
        (ctx, dr) -> {
          val r = ctx.randomness().fhir().createResource();
          dr.addContained(r);
          val message =
              format(
                  "Add random Resource {0} to {1} with ID {2}",
                  r.getClass().getSimpleName(), dr.getClass().getSimpleName(), dr.getId());
          return FuzzingLogEntry.parent(message, ctx.fuzzChild(dr.getClass(), r));
        });

    return mutators;
  }
}
