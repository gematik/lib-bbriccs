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
import org.hl7.fhir.r4.model.*;

public class MedicationRequestMutatorProvider
    extends BaseDomainResourceMutatorProvider<MedicationRequest> {

  public MedicationRequestMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<MedicationRequest>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<MedicationRequest>>();

    mutators.add(
        (ctx, mr) -> ctx.fuzzChildTypes(mr.getClass(), mr.getBasedOn(), mr::getBasedOnFirstRep));
    mutators.add(
        (ctx, mr) -> ctx.fuzzChildTypes(mr.getClass(), mr.getCategory(), mr::getCategoryFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(mr.getClass(), mr.getIdentifier(), mr::getIdentifierFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(mr.getClass(), mr.getDetectedIssue(), mr::getDetectedIssueFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(
                mr.getClass(), mr.getDosageInstruction(), mr::getDosageInstructionFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(mr.getClass(), mr.getEventHistory(), mr::getEventHistoryFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(
                mr.getClass(), mr.getInstantiatesCanonical(), mr::addInstantiatesCanonicalElement));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(
                mr.getClass(), mr.getInstantiatesUri(), mr::addInstantiatesUriElement));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(mr.getClass(), mr.getInsurance(), mr::getInsuranceFirstRep));
    mutators.add((ctx, mr) -> ctx.fuzzChildTypes(mr.getClass(), mr.getNote(), mr::getNoteFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(mr.getClass(), mr.getReasonCode(), mr::getReasonCodeFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(
                mr.getClass(), mr.getReasonReference(), mr::getReasonReferenceFirstRep));
    mutators.add(
        (ctx, mr) ->
            ctx.fuzzChildTypes(
                mr.getClass(),
                mr.getSupportingInformation(),
                mr::getSupportingInformationFirstRep));

    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasPerformerType, mr::getPerformerType));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasPerformer, mr::getPerformer));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasRecorder, mr::getRecorder));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasAuthoredOn, mr::getAuthoredOnElement));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasDoNotPerform, mr::getDoNotPerformElement));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasEncounter, mr::getEncounter));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasGroupIdentifier, mr::getGroupIdentifier));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasIntent, mr::getIntentElement));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasPriority, mr::getPriorityElement));
    mutators.add(
        (ctx, mr) -> ctx.fuzzChild(mr, mr::hasPriorPrescription, mr::getPriorPrescription));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasStatus, mr::getStatusElement));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasStatusReason, mr::getStatusReason));
    mutators.add((ctx, mr) -> ctx.fuzzChild(mr, mr::hasSubject, mr::getSubject));

    mutators.add(
        (ctx, mr) -> {
          if (mr.hasReported()) {
            return ctx.fuzzChild(mr, true, mr::getReported);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(mr::getReportedBooleanType, mr::getReportedReference));
            return ctx.fuzzChild(mr, false, supplier);
          }
        });

    mutators.add(
        (ctx, mr) -> {
          if (mr.hasMedication()) {
            return ctx.fuzzChild(mr, true, mr::getMedication);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(mr::getMedicationReference, mr::getMedicationCodeableConcept));
            return ctx.fuzzChild(mr, false, supplier);
          }
        });

    mutators.add(
        (ctx, medicationRequest) -> {
          val identifiers =
              ctx.randomness()
                  .childResourceDice()
                  .chooseRandomElements(medicationRequest.getIdentifier());
          if (identifiers.isEmpty()) {
            val rnd = ctx.randomness();
            val identifier = medicationRequest.addIdentifier();
            val system = rnd.url(rnd.chooseRandomFromEnum(ResourceType.class), rnd.uuid());
            identifier.setSystem(system);
            identifier.setValue(rnd.regexify(".{10,20}"));
            val message =
                format(
                    "Add Random Identifier to MedicationRequest {0}: system={1} and value={2}",
                    medicationRequest.getId(), identifier.getSystem(), identifier.getValue());
            return FuzzingLogEntry.operation(message);
          } else {
            return ctx.fuzzChildTypes(medicationRequest.getClass(), identifiers);
          }
        });

    mutators.add(
        (ctx, medicationRequest) -> {
          if (medicationRequest.getMedication() == null) {
            medicationRequest.setMedication(new Reference(ctx.randomness().uuid()));
          }
          return ctx.fuzzChild(medicationRequest.getClass(), medicationRequest.getMedication());
        });

    return mutators;
  }
}
