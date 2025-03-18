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
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;

@Getter
public class CanonicalTypeMutatorProvider implements FhirTypeMutatorProvider<CanonicalType> {

  private final List<FuzzingMutator<CanonicalType>> mutators;

  public CanonicalTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<CanonicalType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CanonicalType>>();
    mutators.add((ctx, canonicalType) -> ctx.fuzzIdElement(CanonicalType.class, canonicalType));

    mutators.add(
        (ctx, canonicalType) ->
            ctx.fuzzChildTypes(
                CanonicalType.class,
                ensureNotNull(ctx.randomness(), canonicalType).getExtension()));

    mutators.add(
        (ctx, canonicalType) -> {
          canonicalType = ensureNotNull(ctx.randomness(), canonicalType);
          val url = ctx.randomness().url();
          val type = ctx.randomness().fhir().createType();
          val extensions = new LinkedList<>(canonicalType.getExtension());
          canonicalType.setExtension(extensions);
          canonicalType.addExtension(url, type);
          return FuzzingLogEntry.operation(
              format(
                  "Add random Extension to CanonicalType {0}: {1} / {2}",
                  canonicalType.getId(), url, type));
        });

    return mutators;
  }

  private static CanonicalType ensureNotNull(Randomness randomness, CanonicalType canonicalType) {
    if (canonicalType == null) {
      canonicalType = randomness.fhir().createType(CanonicalType.class);
    }

    return canonicalType;
  }
}
