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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveStringTypes;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Base64BinaryType;

@Getter
public class Base64BinaryTypeMutatorProvider implements FhirTypeMutatorProvider<Base64BinaryType> {

  private final List<FuzzingMutator<Base64BinaryType>> mutators;

  public Base64BinaryTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Base64BinaryType>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Base64BinaryType>>();
    mutators.add((ctx, bt) -> ctx.fuzzIdElement(Base64BinaryType.class, bt));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                Base64BinaryType.class, ensureNotNull(ctx.randomness(), bt).getExtension()));

    mutators.add(
        (ctx, bt) -> {
          bt = ensureNotNull(ctx.randomness(), bt);
          val value = new String(bt.getValue());
          val fresponse =
              ctx.fuzzPrimitiveType(
                  "Fuzz Base64Binary as plain String", PrimitiveStringTypes.TEXT, value);
          bt.setValue(fresponse.getFuzzedValue().getBytes());
          return fresponse.getLogEntry();
        });

    return mutators;
  }

  private static Base64BinaryType ensureNotNull(Randomness randomness, Base64BinaryType b64bt) {
    if (b64bt == null) {
      b64bt = randomness.fhir().createType(Base64BinaryType.class);
    }

    if (!b64bt.hasValue()) {
      val amount = randomness.source().nextInt(1, 100);
      val value = new byte[amount];
      randomness.source().nextBytes(value);
      b64bt.setValue(value);
    }

    return b64bt;
  }
}
