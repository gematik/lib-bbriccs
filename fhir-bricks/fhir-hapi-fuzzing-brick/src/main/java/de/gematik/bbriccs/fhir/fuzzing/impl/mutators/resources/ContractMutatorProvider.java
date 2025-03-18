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
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Contract;
import org.hl7.fhir.r4.model.Type;

public class ContractMutatorProvider extends BaseDomainResourceMutatorProvider<Contract> {

  public ContractMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Contract>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Contract>>();

    mutators.add(
        (ctx, ip) ->
            ctx.fuzzChildTypes(ip.getClass(), ip.getIdentifier(), ip::getIdentifierFirstRep));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getAlias(), ip::addAliasElement));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getSubType(), ip::getSubTypeFirstRep));
    mutators.add(
        (ctx, ip) ->
            ctx.fuzzChildTypes(ip.getClass(), ip.getAuthority(), ip::getAuthorityFirstRep));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getDomain(), ip::getDomainFirstRep));
    mutators.add(
        (ctx, ip) ->
            ctx.fuzzChildTypes(
                ip.getClass(), ip.getRelevantHistory(), ip::getRelevantHistoryFirstRep));
    mutators.add((ctx, ip) -> ctx.fuzzChildTypes(ip.getClass(), ip.getSite(), ip::getSiteFirstRep));

    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasType, ip::getType));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasName, ip::getNameElement));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasExpirationType, ip::getExpirationType));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasApplies, ip::getApplies));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasAuthor, ip::getAuthor));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChild(ip, ip::hasContentDerivative, ip::getContentDerivative));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChild(ip, ip::hasInstantiatesUri, ip::getInstantiatesUriElement));
    mutators.add(
        (ctx, ip) -> ctx.fuzzChild(ip, ip::hasInstantiatesCanonical, ip::getInstantiatesCanonical));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasIssued, ip::getIssuedElement));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasLegalState, ip::getLegalState));
    mutators.add((ctx, ip) -> ctx.fuzzChild(ip, ip::hasScope, ip::getScope));

    mutators.add(
        (ctx, ip) -> {
          if (ip.hasLegallyBinding()) {
            return ctx.fuzzChild(ip, true, ip::getLegallyBinding);
          } else {
            Supplier<Type> supplier =
                ctx.randomness()
                    .chooseRandomElement(
                        List.of(ip::getLegallyBindingAttachment, ip::getLegallyBindingReference));
            return ctx.fuzzChild(ip, false, supplier);
          }
        });

    return mutators;
  }
}
