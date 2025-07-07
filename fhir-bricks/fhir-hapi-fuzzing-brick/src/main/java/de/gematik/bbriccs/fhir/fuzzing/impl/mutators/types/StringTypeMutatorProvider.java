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
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.StringType;

@Getter
public class StringTypeMutatorProvider implements FhirTypeMutatorProvider<StringType> {

  private final List<FuzzingMutator<StringType>> mutators;

  public StringTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<StringType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<StringType>>();
    mutators.add((ctx, stringType) -> ctx.fuzzIdElement(StringType.class, stringType));
    mutators.add(
        (ctx, stringType) ->
            ctx.fuzzChildTypes(
                StringType.class, ensureNotNull(ctx.randomness(), stringType).getExtension()));

    mutators.add(
        (ctx, stringType) -> {
          stringType = ensureNotNull(ctx.randomness(), stringType);
          val value = stringType.getValueNotNull();

          val response =
              ctx.fuzzPrimitiveType(
                  format("Fuzz StringType value {0}", value), PrimitiveStringTypes.TEXT, value);
          stringType.setValue(response.getFuzzedValue());
          return response.getLogEntry();
        });

    return mutators;
  }

  private static StringType ensureNotNull(Randomness randomness, StringType stringType) {
    if (stringType == null) {
      stringType = randomness.fhir().createType(StringType.class);
    }
    return stringType;
  }
}
