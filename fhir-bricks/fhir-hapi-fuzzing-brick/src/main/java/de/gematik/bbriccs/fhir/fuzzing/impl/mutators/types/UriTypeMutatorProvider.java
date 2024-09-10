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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveStringTypes;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.UriType;

@Getter
public class UriTypeMutatorProvider implements FhirTypeMutatorProvider<UriType> {

  private final List<FuzzingMutator<UriType>> mutators;

  public UriTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<UriType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<UriType>>();
    mutators.add((ctx, uriType) -> ctx.fuzzIdElement(UriType.class, uriType));
    mutators.add(
        (ctx, uriType) ->
            ctx.fuzzChildTypes(
                UriType.class, ensureNotNull(ctx.randomness(), uriType).getExtension()));

    mutators.add(
        (ctx, uriType) -> {
          uriType = ensureNotNull(ctx.randomness(), uriType);
          val value = uriType.getValue();
          val response =
              ctx.fuzzPrimitiveType(
                  format("Fuzz UriType value {0}", value), PrimitiveStringTypes.URI, value);
          uriType.setValue(response.getFuzzedValue());
          return response.getLogEntry();
        });

    return mutators;
  }

  private static UriType ensureNotNull(Randomness randomness, UriType uriType) {
    if (uriType == null) {
      uriType = randomness.fhir().createType(UriType.class);
    }
    return uriType;
  }
}
