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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.ResourceType;

@Getter
public class DataRequirementMutatorProvider implements FhirTypeMutatorProvider<DataRequirement> {

  private final List<FuzzingMutator<DataRequirement>> mutators;

  public DataRequirementMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<DataRequirement>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<DataRequirement>>();
    mutators.add((ctx, dr) -> ctx.fuzzIdElement(DataRequirement.class, dr));

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChildTypes(
                DataRequirement.class, ensureNotNull(ctx.randomness(), dr).getExtension()));

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChildTypes(
                DataRequirement.class, ensureNotNull(ctx.randomness(), dr).getMustSupport()));

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChildTypes(
                DataRequirement.class, ensureNotNull(ctx.randomness(), dr).getProfile()));

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChild(
                DataRequirement.class, ensureNotNull(ctx.randomness(), dr).getTypeElement()));

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChild(
                DataRequirement.class, ensureNotNull(ctx.randomness(), dr).getLimitElement()));

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChild(DataRequirement.class, ensureNotNull(ctx.randomness(), dr).getSubject()));

    mutators.add(
        (ctx, dr) -> {
          dr = ensureNotNull(ctx.randomness(), dr);
          val fdr = dr;

          val amount = ctx.randomness().source().nextInt(1, 20);
          val fuzzLogs =
              IntStream.range(0, amount)
                  .mapToObj(
                      idx -> {
                        val s = fdr.addSort();
                        s.setDirection(
                            ctx.randomness()
                                .chooseRandomFromEnum(DataRequirement.SortDirection.class));
                        val pathLen = ctx.randomness().source().nextInt(1, 20);
                        val path =
                            IntStream.range(0, pathLen)
                                .mapToObj(
                                    plidx -> {
                                      if (ctx.randomness().source().nextBoolean()) {
                                        return format("[{0}]", plidx);
                                      } else {
                                        return ctx.randomness()
                                            .chooseRandomFromEnum(ResourceType.class)
                                            .name();
                                      }
                                    })
                                .collect(Collectors.joining("."));
                        s.setPath(path);
                        return FuzzingLogEntry.operation(
                            format(
                                "Add new DataRequirementSortComponent: {0} for {1}",
                                s.getDirection(), path));
                      })
                  .toList();
          return FuzzingLogEntry.parent(
              format(
                  "Add {0} random SortComponents to DataRequirement with ID {1}",
                  amount, fdr.getId()),
              fuzzLogs);
        });

    return mutators;
  }

  private static DataRequirement ensureNotNull(
      Randomness randomness, DataRequirement dataRequirement) {
    if (dataRequirement == null) {
      dataRequirement = randomness.fhir().createType(DataRequirement.class);
    }

    return dataRequirement;
  }
}
