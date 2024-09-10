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
import org.hl7.fhir.r4.model.Annotation;

@Getter
public class AnnotationMutatorProvider implements FhirTypeMutatorProvider<Annotation> {

  private final List<FuzzingMutator<Annotation>> mutators;

  public AnnotationMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Annotation>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Annotation>>();
    mutators.add((ctx, annotation) -> ctx.fuzzIdElement(Annotation.class, annotation));

    mutators.add(
        (ctx, annotation) ->
            ctx.fuzzChildTypes(
                Annotation.class, ensureNotNull(ctx.randomness(), annotation).getExtension()));

    mutators.add(
        (ctx, annotation) ->
            ctx.fuzzChild(
                Annotation.class, ensureNotNull(ctx.randomness(), annotation).getAuthor()));

    mutators.add(
        (ctx, annotation) ->
            ctx.fuzzChild(
                Annotation.class, ensureNotNull(ctx.randomness(), annotation).getTimeElement()));

    mutators.add(
        (ctx, annotation) ->
            ctx.fuzzChild(
                Annotation.class, ensureNotNull(ctx.randomness(), annotation).getTextElement()));

    return mutators;
  }

  private static Annotation ensureNotNull(Randomness randomness, Annotation annotation) {
    if (annotation == null) {
      annotation = randomness.fhir().createType(Annotation.class);
    }

    return annotation;
  }
}
