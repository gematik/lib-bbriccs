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
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;

public class AuditEventMutatorProvider extends BaseDomainResourceMutatorProvider<AuditEvent> {

  public AuditEventMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<AuditEvent>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<AuditEvent>>();

    mutators.add(
        (ctx, ae) ->
            ctx.fuzzChildTypes(
                ae.getClass(), ae.getPurposeOfEvent(), ae::getPurposeOfEventFirstRep));
    mutators.add(
        (ctx, ae) -> ctx.fuzzChildTypes(ae.getClass(), ae.getSubtype(), ae::getSubtypeFirstRep));

    mutators.add((ctx, ae) -> ctx.fuzzChild(ae, ae::hasType, ae::getType));
    mutators.add((ctx, ae) -> ctx.fuzzChild(ae, ae::hasPeriod, ae::getPeriod));
    mutators.add((ctx, ae) -> ctx.fuzzChild(ae, ae::hasOutcome, ae::getOutcomeDescElement));
    mutators.add((ctx, ae) -> ctx.fuzzChild(ae, ae::hasRecorded, ae::getRecordedElement));

    mutators.add(
        (ctx, ae) -> {
          val outcome = ae.getOutcome();
          val foutcome =
              ctx.randomness().chooseRandomFromEnum(AuditEvent.AuditEventOutcome.class, outcome);
          ae.setOutcome(foutcome);
          return FuzzingLogEntry.operation(
              format(
                  "Change AuditEventOutcome of AuditEvent {0}: {1} -> {2}",
                  ae.getId(), outcome, foutcome));
        });

    mutators.add(
        (ctx, ae) -> {
          val action = ae.getAction();
          val faction =
              ctx.randomness().chooseRandomFromEnum(AuditEvent.AuditEventAction.class, action);
          ae.setAction(faction);
          return FuzzingLogEntry.operation(
              format(
                  "Change AuditEventAction of AuditEvent {0}: {1} -> {2}",
                  ae.getId(), action, faction));
        });

    return mutators;
  }
}
