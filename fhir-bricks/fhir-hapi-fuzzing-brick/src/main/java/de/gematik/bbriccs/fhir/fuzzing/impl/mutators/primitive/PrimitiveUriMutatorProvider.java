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
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Getter
@Slf4j
@SuppressWarnings("java:S1192")
public class PrimitiveUriMutatorProvider implements PrimitiveMutatorProvider<String> {

  private static final String HTTP_SCHEMA = "http://";
  private static final String HTTPS_SCHEMA = "https://";

  private final List<PrimitiveTypeMutator<String>> mutators;

  public PrimitiveUriMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<PrimitiveTypeMutator<String>> createMutators() {
    val mutators = new LinkedList<PrimitiveTypeMutator<String>>();

    mutators.add(
        (ctx, input) -> {
          input = ensureNotNull(ctx.randomness(), input);
          val finput = format("{0}|{1}", input, ctx.randomness().version());
          return PrimitiveTypeFuzzingResponse.response(
              finput,
              FuzzingLogEntry.operation(
                  format("Append random Version: {0} -> {1}", input, finput)));
        });

    mutators.add(
        (ctx, input) -> {
          input = ensureNotNull(ctx.randomness(), input);
          val appendix = ctx.randomness().regexify("[A-Za-z0-9-+_/(){}\\[\\]|\"'$%?#!§&=@]{1,20}");
          val finput = format("{0}|{1}", input, appendix);
          return PrimitiveTypeFuzzingResponse.response(
              finput,
              FuzzingLogEntry.operation(
                  format("Append random String instead of Version: {0} -> {1}", input, finput)));
        });

    mutators.add(
        (ctx, input) -> {
          input = ensureNotNull(ctx.randomness(), input);
          val appendix = ctx.randomness().regexify("[|\"'()\\{}\\[\\]]{1,50}");
          val finput = format("{0}|{1}", input, appendix);
          return PrimitiveTypeFuzzingResponse.response(
              finput,
              FuzzingLogEntry.operation(
                  format("Append random Brackets instead of Version: {0} -> {1}", input, finput)));
        });

    mutators.add(
        (ctx, input) -> {
          String fids;
          val originalUrl = input;
          input = ensureNotNull(ctx.randomness(), input);

          if (input.startsWith(HTTP_SCHEMA)) {
            fids = input.replace(HTTP_SCHEMA, HTTPS_SCHEMA);
          } else if (input.startsWith(HTTPS_SCHEMA)) {
            fids = input.replace(HTTPS_SCHEMA, HTTP_SCHEMA);
          } else {
            fids = ctx.randomness().url();
          }
          return PrimitiveTypeFuzzingResponse.response(
              fids,
              FuzzingLogEntry.operation(format("Changed URL: {0} -> {1}", originalUrl, fids)));
        });

    return mutators;
  }

  private static String ensureNotNull(Randomness randomness, @Nullable String uri) {
    if (uri == null) {
      return randomness.url();
    } else {
      return uri;
    }
  }
}
