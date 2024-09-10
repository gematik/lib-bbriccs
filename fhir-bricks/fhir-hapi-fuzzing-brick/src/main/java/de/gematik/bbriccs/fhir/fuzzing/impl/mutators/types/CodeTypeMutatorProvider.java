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
import org.hl7.fhir.r4.model.CodeType;

@Getter
public class CodeTypeMutatorProvider implements FhirTypeMutatorProvider<CodeType> {

  private final List<FuzzingMutator<CodeType>> mutators;

  public CodeTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<CodeType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CodeType>>();
    mutators.add((ctx, codeType) -> ctx.fuzzIdElement(CodeType.class, codeType));

    mutators.add(
        (ctx, codeType) ->
            ctx.fuzzChildTypes(
                CodeType.class, ensureNotNull(ctx.randomness(), codeType).getExtension()));

    mutators.add(
        (ctx, codeType) -> {
          codeType = ensureNotNull(ctx.randomness(), codeType);
          val code = codeType.getCode();

          val response =
              ctx.fuzzPrimitiveType(
                  format("Fuzz CodeType code {0}", code), PrimitiveStringTypes.CODE, code);
          codeType.setValue(response.getFuzzedValue());
          return response.getLogEntry();
        });

    mutators.add(
        (ctx, codeType) -> {
          codeType = ensureNotNull(ctx.randomness(), codeType);
          val system = codeType.getSystem();

          val response =
              ctx.fuzzPrimitiveType(
                  format("Fuzz CodeType system {0}", system), PrimitiveStringTypes.URI, system);
          codeType.setValue(response.getFuzzedValue());
          return response.getLogEntry();
        });

    return mutators;
  }

  private static CodeType ensureNotNull(Randomness randomness, CodeType codeType) {
    if (codeType == null) {
      codeType = randomness.fhir().createType(CodeType.class);
    }
    return codeType;
  }
}
