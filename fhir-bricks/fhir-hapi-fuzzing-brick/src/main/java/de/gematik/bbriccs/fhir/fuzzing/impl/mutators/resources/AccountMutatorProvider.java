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
import org.hl7.fhir.r4.model.Account;

public class AccountMutatorProvider extends BaseDomainResourceMutatorProvider<Account> {

  public AccountMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Account>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Account>>();

    mutators.add(
        (ctx, acc) ->
            ctx.fuzzChildTypes(acc.getClass(), acc.getIdentifier(), acc::getIdentifierFirstRep));
    mutators.add(
        (ctx, acc) ->
            ctx.fuzzChildTypes(acc.getClass(), acc.getSubject(), acc::getSubjectFirstRep));

    mutators.add((ctx, acc) -> ctx.fuzzChild(acc, acc::hasNameElement, acc::getNameElement));
    mutators.add((ctx, acc) -> ctx.fuzzChild(acc, acc::hasType, acc::getType));
    mutators.add((ctx, acc) -> ctx.fuzzChild(acc, acc::hasDescription, acc::getDescriptionElement));
    mutators.add((ctx, acc) -> ctx.fuzzChild(acc, acc::hasOwner, acc::getOwner));
    mutators.add((ctx, acc) -> ctx.fuzzChild(acc, acc::hasPartOf, acc::getPartOf));
    mutators.add((ctx, acc) -> ctx.fuzzChild(acc, acc::hasServicePeriod, acc::getServicePeriod));

    mutators.add(
        (ctx, acc) -> {
          val cov = acc.getCoverageFirstRep();
          return ctx.fuzzChild(acc.getClass(), cov.getCoverageTarget());
        });

    mutators.add(
        (ctx, acc) -> {
          val cov = acc.getCoverageFirstRep();
          return ctx.fuzzChild(acc, cov::hasCoverage, cov::getCoverage);
        });

    return mutators;
  }
}
