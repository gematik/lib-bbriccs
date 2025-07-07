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
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveStringTypes;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;

@Getter
public class IdentifierTypeMutatorProvider implements FhirTypeMutatorProvider<Identifier> {

  private final List<FuzzingMutator<Identifier>> mutators;

  public IdentifierTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Identifier>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Identifier>>();
    mutators.add((ctx, identifier) -> ctx.fuzzIdElement(Identifier.class, identifier));
    mutators.add(
        (ctx, identifier) ->
            ctx.fuzzChildTypes(
                Identifier.class, ensureNotNull(ctx.randomness(), identifier).getExtension()));
    mutators.add(
        (ctx, identifier) -> {
          identifier = ensureNotNull(ctx.randomness(), identifier);
          val system = identifier.getSystem();
          val message = format("Fuzz IdentifierSystem: {0}", system);
          val response = ctx.fuzzPrimitiveType(message, PrimitiveStringTypes.URI, system);
          identifier.setSystem(response.getFuzzedValue());
          return response.getLogEntry();
        });

    mutators.add(
        (ctx, identifier) -> {
          identifier = ensureNotNull(ctx.randomness(), identifier);
          val oids = identifier.getSystem();
          val version = ctx.randomness().version();
          val fids = format("{0}|{1}", identifier.getSystem(), version);
          identifier.setSystem(fids);
          return FuzzingLogEntry.operation(
              format("Append random Version to IdentifierSystem: {0} -> {1}", oids, fids));
        });

    mutators.add(
        (ctx, identifier) -> {
          identifier = ensureNotNull(ctx.randomness(), identifier);
          val oidv = identifier.getValue();
          String fidv;
          if (oidv != null && ctx.randomness().source().nextBoolean()) {
            fidv = format("{0}{1}", oidv.charAt(0), oidv);
          } else if (oidv != null) {
            fidv = format("{0}{1}", oidv, oidv.charAt(oidv.length() - 1));
          } else {
            fidv = ctx.randomness().regexify("[0-9]{5,30}");
          }

          identifier.setValue(fidv);
          return FuzzingLogEntry.operation(
              format("Change Identifier Value: {0} -> {1}", oidv, fidv));
        });

    mutators.add(
        (ctx, identifier) -> {
          identifier = ensureNotNull(ctx.randomness(), identifier);
          val oidv = identifier.getValue();
          identifier.setValue(null);
          return FuzzingLogEntry.operation(format("Remove Value from Identifier: {0}", oidv));
        });

    mutators.add(
        (ctx, identifier) -> {
          identifier = ensureNotNull(ctx.randomness(), identifier);
          val oidv = identifier.getValue();
          val oids = identifier.getSystem();
          identifier.setValue(oids);
          identifier.setSystem(oidv);
          return FuzzingLogEntry.operation(
              format("Swap Identifier Value and System: {0} <-> {1}", oidv, oids));
        });

    mutators.add(
        (ctx, identifier) -> {
          identifier = ensureNotNull(ctx.randomness(), identifier);
          val oids = identifier.getSystem();
          identifier.setSystem(null);
          return FuzzingLogEntry.operation(format("Remove System from Identifier: {0}", oids));
        });

    return mutators;
  }

  private static Identifier ensureNotNull(Randomness randomness, Identifier identifier) {
    if (identifier == null) {
      identifier = randomness.fhir().createType(Identifier.class);
    }
    return identifier;
  }
}
