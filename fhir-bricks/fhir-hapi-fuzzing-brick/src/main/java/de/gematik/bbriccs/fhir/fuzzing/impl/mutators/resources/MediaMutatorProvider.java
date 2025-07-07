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
import java.util.function.Supplier;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Media;
import org.hl7.fhir.r4.model.Type;

@Getter
public class MediaMutatorProvider extends BaseDomainResourceMutatorProvider<Media> {

  public MediaMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Media>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Media>>();

    mutators.add(
        (ctx, media) ->
            ctx.fuzzChildTypes(
                media.getClass(), media.getIdentifier(), media::getIdentifierFirstRep));
    mutators.add(
        (ctx, media) ->
            ctx.fuzzChildTypes(media.getClass(), media.getNote(), media::getNoteFirstRep));
    mutators.add(
        (ctx, media) ->
            ctx.fuzzChildTypes(media.getClass(), media.getPartOf(), media::getPartOfFirstRep));
    mutators.add(
        (ctx, media) ->
            ctx.fuzzChildTypes(media.getClass(), media.getBasedOn(), media::getBasedOnFirstRep));
    mutators.add(
        (ctx, media) ->
            ctx.fuzzChildTypes(
                media.getClass(), media.getReasonCode(), media::getReasonCodeFirstRep));

    mutators.add(
        (ctx, media) -> ctx.fuzzChild(media, media::hasDuration, media::getDurationElement));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasEncounter, media::getEncounter));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasType, media::getType));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasModality, media::getModality));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasFrames, media::getFramesElement));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasHeight, media::getHeightElement));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasWidth, media::getWidthElement));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasIssued, media::getIssuedElement));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasContent, media::getContent));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasDevice, media::getDevice));
    mutators.add((ctx, media) -> ctx.fuzzChild(media, media::hasBodySite, media::getBodySite));

    mutators.add(
        (ctx, media) -> {
          if (media.hasCreated()) {
            return ctx.fuzzChild(media, true, media::getCreated);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(media::getCreatedPeriod, media::getCreatedDateTimeType));
            return ctx.fuzzChild(media, false, supplier);
          }
        });

    mutators.add(
        (ctx, media) -> {
          val status = media.getStatus();
          val fstatus = ctx.randomness().chooseRandomFromEnum(Media.MediaStatus.class, status);
          return FuzzingLogEntry.operation(
              format("Change MediaStatus of {0}: {1} -> {2}", media.getId(), status, fstatus));
        });

    return mutators;
  }
}
