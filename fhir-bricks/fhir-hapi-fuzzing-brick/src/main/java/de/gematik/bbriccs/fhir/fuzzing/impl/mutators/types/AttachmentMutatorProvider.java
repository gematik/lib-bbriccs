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
import org.hl7.fhir.r4.model.Attachment;

@Getter
public class AttachmentMutatorProvider implements FhirTypeMutatorProvider<Attachment> {

  private final List<FuzzingMutator<Attachment>> mutators;

  public AttachmentMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Attachment>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Attachment>>();

    mutators.add((ctx, attachment) -> ctx.fuzzIdElement(Attachment.class, attachment));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChildTypes(
                Attachment.class, ensureNotNull(ctx.randomness(), attachment).getExtension()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class,
                ensureNotNull(ctx.randomness(), attachment).getCreationElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class, ensureNotNull(ctx.randomness(), attachment).getDataElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class,
                ensureNotNull(ctx.randomness(), attachment).getContentTypeElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class, ensureNotNull(ctx.randomness(), attachment).getHashElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class,
                ensureNotNull(ctx.randomness(), attachment).getLanguageElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class, ensureNotNull(ctx.randomness(), attachment).getSizeElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class, ensureNotNull(ctx.randomness(), attachment).getTitleElement()));

    mutators.add(
        (ctx, attachment) ->
            ctx.fuzzChild(
                Attachment.class, ensureNotNull(ctx.randomness(), attachment).getUrlElement()));

    return mutators;
  }

  private static Attachment ensureNotNull(Randomness randomness, Attachment attachment) {
    if (attachment == null) {
      attachment = randomness.fhir().createType(Attachment.class);
    }
    return attachment;
  }
}
