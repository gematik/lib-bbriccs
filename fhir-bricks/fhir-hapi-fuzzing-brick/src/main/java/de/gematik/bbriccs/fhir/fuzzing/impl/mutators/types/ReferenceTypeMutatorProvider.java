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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

@Getter
public class ReferenceTypeMutatorProvider implements FhirTypeMutatorProvider<Reference> {

  private final List<FuzzingMutator<Reference>> mutators;

  public ReferenceTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Reference>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Reference>>();
    mutators.add((ctx, reference) -> ctx.fuzzIdElement(Reference.class, reference));
    mutators.add(
        (ctx, reference) ->
            ctx.fuzzChild(
                Reference.class, ensureNotNull(ctx.randomness(), reference).getTypeElement()));
    mutators.add(
        (ctx, reference) ->
            ctx.fuzzChild(
                Reference.class, ensureNotNull(ctx.randomness(), reference).getDisplayElement()));
    mutators.add(
        (ctx, reference) ->
            ctx.fuzzChild(
                Reference.class,
                ensureNotNull(ctx.randomness(), reference).getReferenceElement_()));
    mutators.add(
        (ctx, reference) ->
            ctx.fuzzChild(
                Reference.class, ensureNotNull(ctx.randomness(), reference).getIdentifier()));
    mutators.add(
        (ctx, reference) ->
            ctx.fuzzChildTypes(
                Reference.class, ensureNotNull(ctx.randomness(), reference).getExtension()));

    mutators.add(
        (ctx, reference) -> {
          reference = ensureNotNull(ctx.randomness(), reference);
          val oidv = reference.getReference();
          val oidvTokens =
              oidv != null ? oidv.split("/") : ctx.randomness().fhir().fhirResourceId().split("/");

          String fidv;
          if (oidvTokens.length == 2) {
            if (ctx.randomness().source().nextBoolean()) {
              // keep the resource part and replace the ID
              fidv = format("{0}/{1}", oidvTokens[0], ctx.randomness().uuid());
            } else {
              // keep the ID and replace the resource type
              val rr = ctx.randomness().chooseRandomFromEnum(ResourceType.class);
              fidv = format("{0}/{1}", rr.toString(), oidvTokens[1]);
            }
          } else {
            fidv = UUID.randomUUID().toString();
          }

          reference.setReference(fidv);
          return FuzzingLogEntry.operation(
              format("Replace Reference value: {0} -> {1}", oidv, fidv));
        });

    return mutators;
  }

  private static Reference ensureNotNull(Randomness randomness, Reference reference) {
    if (reference == null) {
      reference = randomness.fhir().createType(Reference.class);
    }
    return reference;
  }
}
