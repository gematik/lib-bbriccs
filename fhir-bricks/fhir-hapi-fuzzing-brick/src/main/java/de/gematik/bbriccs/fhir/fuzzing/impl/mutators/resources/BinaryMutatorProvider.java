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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveStringTypes;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;

public class BinaryMutatorProvider extends BaseResourceMutatorProvider<Binary> {

  public BinaryMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Binary>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Binary>>();

    mutators.add(
        (ctx, binary) ->
            ctx.fuzzChild(binary, binary::hasContentType, binary::getContentTypeElement));
    mutators.add(
        (ctx, binary) ->
            ctx.fuzzChild(binary, binary::hasSecurityContext, binary::getSecurityContext));

    mutators.add(
        (ctx, binary) -> {
          val b64Data = binary.getDataElement();
          val data = ensureNotNull(ctx.randomness(), b64Data.getValue());
          val fdata = ctx.randomness().regexify(".{1,10}").getBytes();

          byte[] result = new byte[data.length + fdata.length];
          String operation;
          if (ctx.randomness().source().nextBoolean()) {
            operation = "Prepend";
            System.arraycopy(fdata, 0, result, 0, fdata.length);
            System.arraycopy(data, 0, result, fdata.length, data.length);
          } else {
            operation = "Append";
            System.arraycopy(data, 0, result, 0, data.length);
            System.arraycopy(fdata, 0, result, data.length, fdata.length);
          }
          b64Data.setValue(result);
          return FuzzingLogEntry.operation(
              format("{0} random {1} bytes to Binary", operation, fdata.length));
        });

    mutators.add(
        (ctx, binary) -> {
          val b64Data = binary.getDataElement();
          val data = b64Data.getValueAsString();
          val pfResponse =
              ctx.fuzzPrimitiveType("Fuzz Binary as plain String", PrimitiveStringTypes.TEXT, data);
          val fdata = ensureNotNull(ctx.randomness(), pfResponse.getFuzzedValue());
          b64Data.setValue(fdata.getBytes());
          return pfResponse.getLogEntry();
        });

    return mutators;
  }

  private static byte[] ensureNotNull(Randomness randomness, byte[] data) {
    if (data == null || data.length == 0) {
      data = randomness.regexify(".{1,20}").getBytes();
    }
    return data;
  }

  private static String ensureNotNull(Randomness randomness, String data) {
    if (data == null) {
      data = randomness.regexify(".{1,20}");
    }
    return data;
  }
}
