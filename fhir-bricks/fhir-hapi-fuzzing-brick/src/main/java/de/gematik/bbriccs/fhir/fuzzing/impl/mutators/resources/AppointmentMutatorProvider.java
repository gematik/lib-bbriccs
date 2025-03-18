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

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Appointment;

public class AppointmentMutatorProvider extends BaseDomainResourceMutatorProvider<Appointment> {

  public AppointmentMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Appointment>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Appointment>>();

    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(apt.getClass(), apt.getIdentifier(), apt::getIdentifierFirstRep));

    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(apt.getClass(), apt.getServiceType(), apt::getServiceTypeFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(
                apt.getClass(), apt.getServiceCategory(), apt::getServiceCategoryFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(apt.getClass(), apt.getBasedOn(), apt::getBasedOnFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(apt.getClass(), apt.getReasonCode(), apt::getReasonCodeFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(
                apt.getClass(), apt.getReasonReference(), apt::getReasonReferenceFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(
                apt.getClass(), apt.getRequestedPeriod(), apt::getRequestedPeriodFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(apt.getClass(), apt.getSpecialty(), apt::getSpecialtyFirstRep));
    mutators.add(
        (ctx, apt) -> ctx.fuzzChildTypes(apt.getClass(), apt.getSlot(), apt::getSlotFirstRep));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChildTypes(
                apt.getClass(),
                apt.getSupportingInformation(),
                apt::getSupportingInformationFirstRep));

    mutators.add((ctx, apt) -> ctx.fuzzChild(apt, apt::hasPriority, apt::getPriorityElement));
    mutators.add((ctx, apt) -> ctx.fuzzChild(apt, apt::hasCreated, apt::getCreatedElement));
    mutators.add(
        (ctx, apt) -> ctx.fuzzChild(apt, apt::hasAppointmentType, apt::getAppointmentType));
    mutators.add(
        (ctx, apt) -> ctx.fuzzChild(apt, apt::hasCancelationReason, apt::getCancelationReason));
    mutators.add((ctx, apt) -> ctx.fuzzChild(apt, apt::hasComment, apt::getCommentElement));
    mutators.add((ctx, apt) -> ctx.fuzzChild(apt, apt::hasDescription, apt::getDescriptionElement));
    mutators.add((ctx, apt) -> ctx.fuzzChild(apt, apt::hasEnd, apt::getEndElement));
    mutators.add(
        (ctx, apt) -> ctx.fuzzChild(apt, apt::hasMinutesDuration, apt::getMinutesDurationElement));
    mutators.add(
        (ctx, apt) ->
            ctx.fuzzChild(apt, apt::hasPatientInstruction, apt::getPatientInstructionElement));

    return mutators;
  }
}
