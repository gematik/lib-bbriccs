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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class MedicationDispenseMutatorProvider
    extends BaseDomainResourceMutatorProvider<MedicationDispense> {

  public MedicationDispenseMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<MedicationDispense>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<MedicationDispense>>();

    mutators.add(
        (ctx, md) ->
            ctx.fuzzChildTypes(
                md.getClass(),
                md.getAuthorizingPrescription(),
                md::getAuthorizingPrescriptionFirstRep));
    mutators.add(
        (ctx, md) ->
            ctx.fuzzChildTypes(md.getClass(), md.getDetectedIssue(), md::getDetectedIssueFirstRep));
    mutators.add(
        (ctx, md) ->
            ctx.fuzzChildTypes(
                md.getClass(), md.getDosageInstruction(), md::getDosageInstructionFirstRep));
    mutators.add(
        (ctx, md) ->
            ctx.fuzzChildTypes(md.getClass(), md.getEventHistory(), md::getEventHistoryFirstRep));
    mutators.add(
        (ctx, md) ->
            ctx.fuzzChildTypes(md.getClass(), md.getIdentifier(), md::getIdentifierFirstRep));
    mutators.add(
        (ctx, md) -> ctx.fuzzChildTypes(md.getClass(), md.getPartOf(), md::getPartOfFirstRep));
    mutators.add(
        (ctx, md) -> ctx.fuzzChildTypes(md.getClass(), md.getReceiver(), md::getReceiverFirstRep));
    mutators.add((ctx, md) -> ctx.fuzzChildTypes(md.getClass(), md.getNote(), md::getNoteFirstRep));
    mutators.add(
        (ctx, md) ->
            ctx.fuzzChildTypes(
                md.getClass(),
                md.getSupportingInformation(),
                md::getSupportingInformationFirstRep));

    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasType, md::getType));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasCategory, md::getCategory));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasContext, md::getContext));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasDaysSupply, md::getDaysSupply));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasDestination, md::getDestination));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasLocation, md::getLocation));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasQuantity, md::getQuantity));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasSubject, md::getSubject));
    mutators.add(
        (ctx, md) -> ctx.fuzzChild(md, md::hasWhenHandedOver, md::getWhenHandedOverElement));
    mutators.add((ctx, md) -> ctx.fuzzChild(md, md::hasWhenPrepared, md::getWhenPreparedElement));

    mutators.add(
        (ctx, md) -> {
          if (md.getMedication() == null) {
            md.setMedication(new Reference(ctx.randomness().uuid()));
          }
          return ctx.fuzzChild(md.getClass(), md.getMedication());
        });

    mutators.add(
        (ctx, md) -> {
          if (md.hasStatusReason()) {
            return ctx.fuzzChild(md, true, md::getStatusReason);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(md::getStatusReasonReference, md::getStatusReasonCodeableConcept));
            return ctx.fuzzChild(md, false, supplier);
          }
        });

    mutators.add(
        (ctx, md) -> {
          val original = md.getStatus();

          val status =
              ctx.randomness()
                  .chooseRandomFromEnum(
                      MedicationDispense.MedicationDispenseStatus.class, original);
          md.setStatus(status);
          return FuzzingLogEntry.operation(
              format(
                  "Change Status of MedicationDispense {0}: {1} -> {2}",
                  md.getId(), original, status));
        });

    return mutators;
  }
}
