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
import org.hl7.fhir.r4.model.*;

public class MedicationMutatorProvider extends BaseDomainResourceMutatorProvider<Medication> {

  public MedicationMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Medication>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Medication>>();

    mutators.add(
        (ctx, medication) ->
            ctx.fuzzChildTypes(
                medication.getClass(),
                medication.getIdentifier(),
                medication::getIdentifierFirstRep));

    mutators.add(
        (ctx, medication) ->
            ctx.fuzzChild(medication, medication::hasAmount, medication::getAmount));
    mutators.add(
        (ctx, medication) -> ctx.fuzzChild(medication, medication::hasForm, medication::getForm));
    mutators.add(
        (ctx, medication) -> ctx.fuzzChild(medication, medication::hasCode, medication::getCode));
    mutators.add(
        (ctx, medication) ->
            ctx.fuzzChild(medication, medication::hasManufacturer, medication::getManufacturer));

    mutators.add(
        (ctx, medication) -> {
          val rnd = ctx.randomness();
          val identifiers =
              rnd.childResourceDice().chooseRandomElements(medication.getIdentifier());
          if (identifiers.isEmpty()) {
            val identifier = medication.addIdentifier();
            val system = rnd.url(rnd.chooseRandomFromEnum(ResourceType.class), rnd.uuid());
            identifier.setSystem(system);
            identifier.setValue(rnd.regexify(".{10,20}"));
            val message =
                format(
                    "Add Random Identifier to Medication {0}: system={1} and value={2}",
                    medication.getId(), identifier.getSystem(), identifier.getValue());
            return FuzzingLogEntry.operation(message);
          } else {
            return ctx.fuzzChildTypes(medication.getClass(), identifiers);
          }
        });

    return mutators;
  }
}
