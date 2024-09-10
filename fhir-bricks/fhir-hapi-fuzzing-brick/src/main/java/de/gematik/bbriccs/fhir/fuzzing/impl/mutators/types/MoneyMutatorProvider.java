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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Money;

@Getter
public class MoneyMutatorProvider implements FhirTypeMutatorProvider<Money> {

  private final List<FuzzingMutator<Money>> mutators;

  public MoneyMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Money>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Money>>();
    mutators.add((ctx, money) -> ctx.fuzzIdElement(Money.class, money));
    mutators.add(
        (ctx, money) ->
            ctx.fuzzChildTypes(Money.class, ensureNotNull(ctx.randomness(), money).getExtension()));
    mutators.add(
        (ctx, money) ->
            ctx.fuzzChild(
                Money.class, ensureNotNull(ctx.randomness(), money).getCurrencyElement()));
    mutators.add(
        (ctx, money) ->
            ctx.fuzzChild(Money.class, ensureNotNull(ctx.randomness(), money).getValueElement()));

    return mutators;
  }

  private static Money ensureNotNull(Randomness randomness, Money money) {
    if (money == null) {
      money = randomness.fhir().createType(Money.class);
    }
    return money;
  }
}
