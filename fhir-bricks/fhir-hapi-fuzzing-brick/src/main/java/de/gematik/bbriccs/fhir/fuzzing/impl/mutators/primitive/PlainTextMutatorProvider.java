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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.primitive;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.*;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.val;

@Getter
public class PlainTextMutatorProvider implements PrimitiveMutatorProvider<String> {

  private final List<PrimitiveTypeMutator<String>> mutators;

  public PlainTextMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<PrimitiveTypeMutator<String>> createMutators() {
    val mutators = new LinkedList<PrimitiveTypeMutator<String>>();

    mutators.add(
        (ctx, text) -> {
          val otext = ensureNotNull(ctx.randomness(), text);
          val reversed = new StringBuilder(otext).reverse().toString();
          return PrimitiveTypeFuzzingResponse.response(
              reversed,
              FuzzingLogEntry.operation(
                  format("Reverse Text: ''{0}'' -> ''{1}''", text, reversed)));
        });

    mutators.add(
        (ctx, text) -> {
          text = ensureNotNull(ctx.randomness(), text);
          val duplicated = text + text;
          return PrimitiveTypeFuzzingResponse.response(
              duplicated,
              FuzzingLogEntry.operation(
                  format("Duplicate Text: ''{0}'' -> ''{1}''", text, duplicated)));
        });

    mutators.add(
        (ctx, text) -> {
          text = ensureNotNull(ctx.randomness(), text);
          val empty = " ";
          return PrimitiveTypeFuzzingResponse.response(
              empty,
              FuzzingLogEntry.operation(format("Make empty Text: {0} -> ''{1}''", text, empty)));
        });

    mutators.add(
        (ctx, text) -> {
          val spaces = ctx.randomness().source().nextInt(1, 5);
          val builder = new StringBuilder();
          IntStream.range(0, spaces)
              .forEach(
                  i ->
                      builder.append(
                          ctx.randomness().chooseRandomElement(List.of(" ", "\t", "\n"))));

          val ftext = builder.toString();
          return PrimitiveTypeFuzzingResponse.response(
              ftext,
              FuzzingLogEntry.operation(
                  format("Make empty Text with random white spaces: {0} -> ''{1}''", text, ftext)));
        });

    return mutators;
  }

  private static String ensureNotNull(Randomness randomness, String text) {
    if (text == null || text.isEmpty() || text.isBlank()) {
      return randomness.regexify("[A-Za-z]{2,50}");
    } else {
      return text;
    }
  }
}
