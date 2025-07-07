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

import de.gematik.bbriccs.fhir.fuzzing.FhirType;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;

@Getter
public class ConsentMutatorProvider extends BaseDomainResourceMutatorProvider<Consent> {

  public ConsentMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Consent>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Consent>>();

    mutators.add(
        (ctx, consent) ->
            ctx.fuzzChildTypes(
                consent.getClass(), consent.getIdentifier(), consent::getIdentifierFirstRep));
    mutators.add(
        (ctx, consent) ->
            ctx.fuzzChildTypes(
                consent.getClass(), consent.getCategory(), consent::getCategoryFirstRep));
    mutators.add(
        (ctx, consent) ->
            ctx.fuzzChildTypes(
                consent.getClass(), consent.getOrganization(), consent::getOrganizationFirstRep));
    mutators.add(
        (ctx, consent) ->
            ctx.fuzzChildTypes(
                consent.getClass(), consent.getPerformer(), consent::getPerformerFirstRep));

    mutators.add(
        (ctx, consent) -> ctx.fuzzChild(consent, consent::hasPatient, consent::getPatient));
    mutators.add(
        (ctx, consent) ->
            ctx.fuzzChild(consent, consent::hasDateTimeElement, consent::getDateTimeElement));
    mutators.add(
        (ctx, consent) -> ctx.fuzzChild(consent, consent::hasPolicyRule, consent::getPolicyRule));
    mutators.add((ctx, consent) -> ctx.fuzzChild(consent, consent::hasScope, consent::getScope));

    mutators.add(
        (ctx, consent) -> {
          if (!consent.hasSource()) {
            val sourceType =
                ctx.randomness()
                    .chooseRandomElement(List.of(FhirType.ATTACHMENT, FhirType.REFERENCE));
            val source = ctx.randomness().fhir().createType(sourceType);
            consent.setSource(source);
          }
          return ctx.fuzzChild(consent.getClass(), consent.getSource());
        });

    mutators.add(
        (ctx, consent) ->
            ctx.randomness()
                .chooseRandomly(consent.getPolicy())
                .map(p -> ctx.fuzzChild(consent.getClass(), p.getAuthorityElement()))
                .orElseGet(
                    () ->
                        FuzzingLogEntry.noop(
                            format(
                                "Consent {0} does not have a policy to fuzz", consent.getId()))));

    mutators.add(
        (ctx, consent) -> {
          val status = consent.getStatus();
          val fstatus = ctx.randomness().chooseRandomFromEnum(Consent.ConsentState.class, status);
          consent.setStatus(fstatus);
          return FuzzingLogEntry.operation(
              format("Change Status of Consent {0}: {1} -> {2}", consent.getId(), status, fstatus));
        });

    return mutators;
  }
}
