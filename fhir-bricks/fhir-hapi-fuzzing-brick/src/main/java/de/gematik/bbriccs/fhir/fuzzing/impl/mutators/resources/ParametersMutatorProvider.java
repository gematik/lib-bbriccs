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
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;

public class ParametersMutatorProvider extends BaseResourceMutatorProvider<Parameters> {

  public ParametersMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Parameters>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Parameters>>();

    mutators.add(
        (ctx, parameters) -> {
          parameters.setParameter(List.of());
          return FuzzingLogEntry.operation(
              format("Remove parameters from {0}", parameters.getId()));
        });

    mutators.add(
        (ctx, parameters) -> {
          val p = new LinkedList<Parameters.ParametersParameterComponent>();
          val amount = ctx.randomness().source().nextInt(1, 20);
          IntStream.rangeClosed(0, amount)
              .forEach(
                  idx -> {
                    val stc = new StringType(ctx.randomness().url());
                    val ppc = new Parameters.ParametersParameterComponent(stc);
                    ppc.setName(ctx.randomness().fhir().fhirResourceId());
                    p.add(ppc);
                  });
          parameters.setParameter(p);
          return FuzzingLogEntry.operation(
              format(
                  "Create randomly {0} ParameterComponents for {1}", amount, parameters.getId()));
        });

    return mutators;
  }
}
