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

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItemDefinition;

public class ChargeItemDefinitionMutatorProvider
    extends BaseDomainResourceMutatorProvider<ChargeItemDefinition> {

  public ChargeItemDefinitionMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<ChargeItemDefinition>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ChargeItemDefinition>>();

    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(cid.getClass(), cid.getIdentifier(), cid::getIdentifierFirstRep));
    mutators.add(
        (ctx, cid) -> ctx.fuzzChildTypes(cid.getClass(), cid.getPartOf(), cid::addPartOfElement));
    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(cid.getClass(), cid.getContact(), cid::getContactFirstRep));
    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(
                cid.getClass(), cid.getDerivedFromUri(), cid::addDerivedFromUriElement));
    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(cid.getClass(), cid.getInstance(), cid::getInstanceFirstRep));
    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(
                cid.getClass(), cid.getJurisdiction(), cid::getJurisdictionFirstRep));
    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(cid.getClass(), cid.getReplaces(), cid::addReplacesElement));
    mutators.add(
        (ctx, cid) ->
            ctx.fuzzChildTypes(cid.getClass(), cid.getUseContext(), cid::getUseContextFirstRep));

    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasCopyright, cid::getCopyrightElement));
    mutators.add(
        (ctx, cid) -> ctx.fuzzChild(cid, cid::hasApprovalDate, cid::getApprovalDateElement));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasDate, cid::getDateElement));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasCode, cid::getCode));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasDescription, cid::getDescriptionElement));
    mutators.add(
        (ctx, cid) -> ctx.fuzzChild(cid, cid::hasEffectivePeriod, cid::getEffectivePeriod));
    mutators.add(
        (ctx, cid) -> ctx.fuzzChild(cid, cid::hasLastReviewDate, cid::getLastReviewDateElement));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasPublisher, cid::getPublisherElement));
    mutators.add(
        (ctx, cid) -> ctx.fuzzChild(cid, cid::hasExperimental, cid::getExperimentalElement));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasTitle, cid::getTitleElement));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasUrl, cid::getUrlElement));
    mutators.add((ctx, cid) -> ctx.fuzzChild(cid, cid::hasVersion, cid::getVersionElement));

    return mutators;
  }
}
