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
import org.hl7.fhir.r4.model.RelatedArtifact;

@Getter
public class RelatedArtifactMutatorProvider implements FhirTypeMutatorProvider<RelatedArtifact> {

  private final List<FuzzingMutator<RelatedArtifact>> mutators;

  public RelatedArtifactMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<RelatedArtifact>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<RelatedArtifact>>();
    mutators.add((ctx, ra) -> ctx.fuzzIdElement(RelatedArtifact.class, ra));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChildTypes(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getExtension()));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChild(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getCitationElement()));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChild(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getResourceElement()));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChild(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getDisplayElement()));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChild(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getDocument()));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChild(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getLabelElement()));

    mutators.add(
        (ctx, ra) ->
            ctx.fuzzChild(
                RelatedArtifact.class, ensureNotNull(ctx.randomness(), ra).getUrlElement()));

    return mutators;
  }

  private static RelatedArtifact ensureNotNull(Randomness randomness, RelatedArtifact booleanType) {
    if (booleanType == null) {
      booleanType = randomness.fhir().createType(RelatedArtifact.class);
    }

    return booleanType;
  }
}
