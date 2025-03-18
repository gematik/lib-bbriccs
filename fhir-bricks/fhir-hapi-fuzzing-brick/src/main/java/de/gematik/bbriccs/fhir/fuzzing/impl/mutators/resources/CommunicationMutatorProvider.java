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
import org.hl7.fhir.r4.model.Communication;

public class CommunicationMutatorProvider extends BaseDomainResourceMutatorProvider<Communication> {

  public CommunicationMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Communication>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Communication>>();

    mutators.add((ctx, com) -> ctx.fuzzChild(com, com::hasEncounter, com::getEncounter));

    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(com.getClass(), com.getBasedOn(), com::getBasedOnFirstRep));
    mutators.add(
        (ctx, com) -> ctx.fuzzChildTypes(com.getClass(), com.getAbout(), com::getAboutFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(com.getClass(), com.getIdentifier(), com::getIdentifierFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(
                com.getClass(), com.getInResponseTo(), com::getInResponseToFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(
                com.getClass(), com.getInstantiatesUri(), com::addInstantiatesUriElement));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(
                com.getClass(),
                com.getInstantiatesCanonical(),
                com::addInstantiatesCanonicalElement));
    mutators.add(
        (ctx, com) -> ctx.fuzzChildTypes(com.getClass(), com.getMedium(), com::getMediumFirstRep));
    mutators.add(
        (ctx, com) -> ctx.fuzzChildTypes(com.getClass(), com.getNote(), com::getNoteFirstRep));
    mutators.add(
        (ctx, com) -> ctx.fuzzChildTypes(com.getClass(), com.getPartOf(), com::getPartOfFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(com.getClass(), com.getReasonCode(), com::getReasonCodeFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(
                com.getClass(), com.getReasonReference(), com::getReasonReferenceFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(com.getClass(), com.getRecipient(), com::getRecipientFirstRep));
    mutators.add(
        (ctx, com) -> ctx.fuzzChildTypes(com.getClass(), com.getAbout(), com::getAboutFirstRep));
    mutators.add(
        (ctx, com) ->
            ctx.fuzzChildTypes(com.getClass(), com.getCategory(), com::getCategoryFirstRep));

    mutators.add(
        (ctx, com) -> {
          val payloads =
              com.getPayload().stream()
                  .map(Communication.CommunicationPayloadComponent::getContent)
                  .toList();
          return ctx.fuzzChildTypes(com.getClass(), payloads);
        });

    mutators.add(
        (ctx, com) -> {
          val priority = com.getPriority();
          val fpriority =
              ctx.randomness()
                  .chooseRandomFromEnum(Communication.CommunicationPriority.class, priority);
          com.setPriority(fpriority);
          return FuzzingLogEntry.operation(
              format(
                  "Change Priority of Communication {0}: {1} -> {2}",
                  com.getId(), priority, fpriority));
        });

    return mutators;
  }
}
