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
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerMutatorProvider extends BaseDomainResourceMutatorProvider<Practitioner> {

  public PractitionerMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Practitioner>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Practitioner>>();

    mutators.add(
        (ctx, pract) ->
            ctx.fuzzChildTypes(pract.getClass(), pract.getAddress(), pract::getAddressFirstRep));
    mutators.add(
        (ctx, pract) ->
            ctx.fuzzChildTypes(
                pract.getClass(), pract.getIdentifier(), pract::getIdentifierFirstRep));
    mutators.add(
        (ctx, pract) ->
            ctx.fuzzChildTypes(pract.getClass(), pract.getName(), pract::getNameFirstRep));
    mutators.add(
        (ctx, pract) ->
            ctx.fuzzChildTypes(
                pract.getClass(), pract.getCommunication(), pract::getCommunicationFirstRep));
    mutators.add(
        (ctx, pract) ->
            ctx.fuzzChildTypes(pract.getClass(), pract.getPhoto(), pract::getPhotoFirstRep));
    mutators.add(
        (ctx, pract) ->
            ctx.fuzzChildTypes(pract.getClass(), pract.getTelecom(), pract::getTelecomFirstRep));

    mutators.add(
        (ctx, pract) -> {
          val qts = pract.getQualification();
          return ctx.randomness()
              .chooseRandomly(qts)
              .map(qt -> ctx.fuzzChild(pract.getClass(), qt.getCode()))
              .orElseGet(
                  () -> {
                    val qtComponent = new Practitioner.PractitionerQualificationComponent();
                    qtComponent.getCode().setText(ctx.randomness().regexify("[a-zA-Z ]{10,20}"));
                    qts.add(qtComponent);
                    return FuzzingLogEntry.operation(
                        format(
                            "Add random PractitionerQualification with text ''{0}''",
                            qtComponent.getCode().getText()));
                  });
        });

    return mutators;
  }
}
