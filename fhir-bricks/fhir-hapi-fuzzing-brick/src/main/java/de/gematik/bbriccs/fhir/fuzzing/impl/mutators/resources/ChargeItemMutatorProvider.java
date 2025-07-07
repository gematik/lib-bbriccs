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
import org.hl7.fhir.r4.model.*;

public class ChargeItemMutatorProvider extends BaseDomainResourceMutatorProvider<ChargeItem> {

  public ChargeItemMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<ChargeItem>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ChargeItem>>();

    mutators.add(
        (ctx, ci) ->
            ctx.fuzzChildTypes(ci.getClass(), ci.getIdentifier(), ci::getIdentifierFirstRep));
    mutators.add(
        (ctx, ci) -> ctx.fuzzChildTypes(ci.getClass(), ci.getAccount(), ci::getAccountFirstRep));
    mutators.add(
        (ctx, ci) -> ctx.fuzzChildTypes(ci.getClass(), ci.getBodysite(), ci::getBodysiteFirstRep));
    mutators.add((ctx, ci) -> ctx.fuzzChildTypes(ci.getClass(), ci.getNote(), ci::getNoteFirstRep));
    mutators.add(
        (ctx, ci) ->
            ctx.fuzzChildTypes(
                ci.getClass(), ci.getDefinitionCanonical(), ci::addDefinitionCanonicalElement));
    mutators.add(
        (ctx, ci) ->
            ctx.fuzzChildTypes(ci.getClass(), ci.getDefinitionUri(), ci::addDefinitionUriElement));
    mutators.add(
        (ctx, ci) -> ctx.fuzzChildTypes(ci.getClass(), ci.getPartOf(), ci::getPartOfFirstRep));
    mutators.add(
        (ctx, ci) -> ctx.fuzzChildTypes(ci.getClass(), ci.getReason(), ci::getReasonFirstRep));
    mutators.add(
        (ctx, ci) -> ctx.fuzzChildTypes(ci.getClass(), ci.getService(), ci::getServiceFirstRep));
    mutators.add(
        (ctx, ci) ->
            ctx.fuzzChildTypes(
                ci.getClass(),
                ci.getSupportingInformation(),
                ci::getSupportingInformationFirstRep));

    mutators.add((ctx, ci) -> ctx.fuzzChild(ci, ci::hasCode, ci::getCode));
    mutators.add((ctx, ci) -> ctx.fuzzChild(ci, ci::hasContext, ci::getContext));
    mutators.add((ctx, ci) -> ctx.fuzzChild(ci, ci::hasCostCenter, ci::getCostCenter));
    mutators.add((ctx, ci) -> ctx.fuzzChild(ci, ci::hasEnteredDate, ci::getEnteredDateElement));
    mutators.add((ctx, ci) -> ctx.fuzzChild(ci, ci::hasEnterer, ci::getEnterer));
    mutators.add(
        (ctx, ci) -> ctx.fuzzChild(ci, ci::hasFactorOverride, ci::getFactorOverrideElement));

    return mutators;
  }
}
