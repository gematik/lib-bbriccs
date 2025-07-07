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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceType;

@Getter
public class IdTypeMutatorProvider implements FhirTypeMutatorProvider<IdType> {

  private final List<FuzzingMutator<IdType>> mutators;

  public IdTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<IdType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<IdType>>();
    mutators.add((ctx, idt) -> ctx.fuzzIdElement(IdType.class, idt));
    mutators.add(
        (ctx, idt) ->
            ctx.fuzzChildTypes(IdType.class, ensureNotNull(ctx.randomness(), idt).getExtension()));
    mutators.add(
        (ctx, idt) -> {
          idt = ensureNotNull(ctx.randomness(), idt);
          val oidv = idt.getValue();
          val oidvTokens =
              oidv != null ? oidv.split("/") : ctx.randomness().fhir().fhirResourceId().split("/");

          String fidv;
          if (oidvTokens.length == 2) {
            if (ctx.randomness().source().nextBoolean()) {
              // keep the resource part and replace the ID
              fidv = format("{0}/{1}", oidvTokens[0], ctx.randomness().uuid());
            } else {
              // keep the ID and replace the resource type
              val rr = ctx.randomness().chooseRandomFromEnum(ResourceType.class);
              fidv = format("{0}/{1}", rr.toString(), oidvTokens[1]);
            }
          } else if (oidv.startsWith("http")) {
            // some kind of URL: replace the last token which usually contains an ID
            fidv = oidv.replace(oidvTokens[oidvTokens.length - 1], ctx.randomness().uuid());
          } else {
            fidv = UUID.randomUUID().toString();
          }

          idt.setValue(fidv);

          return FuzzingLogEntry.operation(
              format("Replace IdElement value: {0} -> {1}", oidv, fidv));
        });

    return mutators;
  }

  private static IdType ensureNotNull(Randomness randomness, IdType idType) {
    if (idType == null) {
      idType = randomness.fhir().createType(IdType.class);
    }
    return idType;
  }
}
