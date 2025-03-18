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
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.CommunicationRequest;
import org.hl7.fhir.r4.model.Type;

public class CommunicationRequestMutatorProvider
    extends BaseDomainResourceMutatorProvider<CommunicationRequest> {

  public CommunicationRequestMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<CommunicationRequest>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CommunicationRequest>>();

    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(comreq.getClass(), comreq.getBasedOn(), comreq::getBasedOnFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(comreq.getClass(), comreq.getAbout(), comreq::getAboutFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(
                comreq.getClass(), comreq.getIdentifier(), comreq::getIdentifierFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(comreq.getClass(), comreq.getMedium(), comreq::getMediumFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(
                comreq.getClass(), comreq.getReasonCode(), comreq::getReasonCodeFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(comreq.getClass(), comreq.getNote(), comreq::getNoteFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(
                comreq.getClass(),
                comreq.getReasonReference(),
                comreq::getReasonReferenceFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(
                comreq.getClass(), comreq.getRecipient(), comreq::getRecipientFirstRep));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChildTypes(
                comreq.getClass(), comreq.getCategory(), comreq::getCategoryFirstRep));

    mutators.add(
        (ctx, comreq) -> ctx.fuzzChild(comreq, comreq::hasEncounter, comreq::getEncounter));
    mutators.add(
        (ctx, comreq) -> ctx.fuzzChild(comreq, comreq::hasRequester, comreq::getRequester));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChild(comreq, comreq::hasDoNotPerform, comreq::getDoNotPerformElement));
    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChild(comreq, comreq::hasGroupIdentifier, comreq::getGroupIdentifier));
    mutators.add((ctx, comreq) -> ctx.fuzzChild(comreq, comreq::hasSender, comreq::getSender));

    mutators.add(
        (ctx, comreq) ->
            ctx.fuzzChild(comreq, comreq::hasAuthoredOn, comreq::getAuthoredOnElement));

    mutators.add(
        (ctx, comreq) -> {
          if (comreq.hasOccurrence()) {
            return ctx.fuzzChild(comreq, comreq::hasOccurrencePeriod, comreq::getOccurrence);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(comreq::getOccurrencePeriod, comreq::getOccurrenceDateTimeType));
            return ctx.fuzzChild(comreq, comreq::hasOccurrence, supplier);
          }
        });

    mutators.add(
        (ctx, comreq) -> {
          val payloads =
              comreq.getPayload().stream()
                  .map(CommunicationRequest.CommunicationRequestPayloadComponent::getContent)
                  .toList();
          return ctx.fuzzChildTypes(comreq.getClass(), payloads);
        });

    mutators.add(
        (ctx, comreq) -> {
          val priority = comreq.getPriority();
          val fpriority =
              ctx.randomness()
                  .chooseRandomFromEnum(CommunicationRequest.CommunicationPriority.class, priority);
          comreq.setPriority(fpriority);
          return FuzzingLogEntry.operation(
              format(
                  "Change Priority of CommunicationRequest {0}: {1} -> {2}",
                  comreq.getId(), priority, fpriority));
        });

    return mutators;
  }
}
