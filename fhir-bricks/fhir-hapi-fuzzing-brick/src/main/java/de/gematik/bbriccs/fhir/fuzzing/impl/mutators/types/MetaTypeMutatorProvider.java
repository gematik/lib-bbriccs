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
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Meta;

@Getter
public class MetaTypeMutatorProvider implements FhirTypeMutatorProvider<Meta> {

  private final List<FuzzingMutator<Meta>> mutators;

  public MetaTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Meta>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Meta>>();
    mutators.add((ctx, meta) -> ctx.fuzzIdElement(Meta.class, meta));
    mutators.add(
        (ctx, meta) ->
            ctx.fuzzChild(Meta.class, ensureNotNull(ctx.randomness(), meta).getVersionIdElement()));
    mutators.add(
        (ctx, meta) ->
            ctx.fuzzChildTypes(Meta.class, ensureNotNull(ctx.randomness(), meta).getExtension()));
    mutators.add(
        (ctx, meta) ->
            ctx.fuzzChildTypes(Meta.class, ensureNotNull(ctx.randomness(), meta).getSecurity()));
    mutators.add(
        (ctx, meta) ->
            ctx.fuzzChildTypes(Meta.class, ensureNotNull(ctx.randomness(), meta).getProfile()));
    mutators.add(
        (ctx, meta) ->
            ctx.fuzzChildTypes(Meta.class, ensureNotNull(ctx.randomness(), meta).getTag()));

    mutators.add(
        (ctx, meta) -> {
          meta = ensureNotNull(ctx.randomness(), meta);
          val amount = ctx.randomness().source().nextInt(1, 2);
          val children = new LinkedList<FuzzingLogEntry>();
          for (var i = 0; i < amount; i++) {
            val uuid = ctx.randomness().uuid();
            val profile = new CanonicalType(ctx.randomness().url(uuid));
            meta.getProfile().add(profile);
            children.add(ctx.fuzzChild("Fuzz new random Profile for Meta", profile));
          }

          return FuzzingLogEntry.parent("Add randomly Profiles to Meta", children);
        });

    return mutators;
  }

  private static Meta ensureNotNull(Randomness randomness, Meta meta) {
    if (meta == null) {
      meta = randomness.fhir().createType(Meta.class);
    }
    return meta;
  }
}
