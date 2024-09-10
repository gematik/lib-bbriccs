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
import org.hl7.fhir.r4.model.Expression;

@Getter
public class ExpressionMutatorProvider implements FhirTypeMutatorProvider<Expression> {

  private final List<FuzzingMutator<Expression>> mutators;

  public ExpressionMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Expression>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Expression>>();
    mutators.add((ctx, expression) -> ctx.fuzzIdElement(Expression.class, expression));
    mutators.add(
        (ctx, expression) ->
            ctx.fuzzChildTypes(
                Expression.class, ensureNotNull(ctx.randomness(), expression).getExtension()));

    mutators.add(
        (ctx, expression) ->
            ctx.fuzzChild(
                Expression.class,
                ensureNotNull(ctx.randomness(), expression).getExpressionElement()));

    mutators.add(
        (ctx, expression) ->
            ctx.fuzzChild(
                Expression.class,
                ensureNotNull(ctx.randomness(), expression).getDescriptionElement()));

    mutators.add(
        (ctx, expression) ->
            ctx.fuzzChild(
                Expression.class,
                ensureNotNull(ctx.randomness(), expression).getLanguageElement()));

    mutators.add(
        (ctx, expression) ->
            ctx.fuzzChild(
                Expression.class, ensureNotNull(ctx.randomness(), expression).getNameElement()));

    mutators.add(
        (ctx, expression) ->
            ctx.fuzzChild(
                Expression.class,
                ensureNotNull(ctx.randomness(), expression).getReferenceElement()));

    return mutators;
  }

  private static Expression ensureNotNull(Randomness randomness, Expression expression) {
    if (expression == null) {
      expression = randomness.fhir().createType(Expression.class);
    }

    return expression;
  }
}
