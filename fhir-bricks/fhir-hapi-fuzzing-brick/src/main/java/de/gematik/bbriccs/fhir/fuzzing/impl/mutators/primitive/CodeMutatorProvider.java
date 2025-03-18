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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.primitive;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.*;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.val;

@Getter
public class CodeMutatorProvider implements PrimitiveMutatorProvider<String> {

  private final List<PrimitiveTypeMutator<String>> mutators;

  public CodeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<PrimitiveTypeMutator<String>> createMutators() {
    val mutators = new LinkedList<PrimitiveTypeMutator<String>>();

    mutators.add(
        (ctx, code) ->
            ctx.fuzzPrimitiveType(
                format("Fuzz Code {0} as plain text", code),
                PrimitiveStringTypes.TEXT,
                ensureNotNull(ctx.randomness(), code)));

    return mutators;
  }

  private static String ensureNotNull(Randomness randomness, @Nullable String code) {
    if (code == null) {
      return randomness.regexify("[A-Za-z]{2,5}");
    } else {
      return code;
    }
  }
}
