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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Signature;

@Getter
public class SignatureMutatorProvider implements FhirTypeMutatorProvider<Signature> {

  private final List<FuzzingMutator<Signature>> mutators;

  public SignatureMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Signature>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Signature>>();
    mutators.add((ctx, signature) -> ctx.fuzzIdElement(Signature.class, signature));
    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChildTypes(
                Signature.class, ensureNotNull(ctx.randomness(), signature).getExtension()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChildTypes(
                Signature.class, ensureNotNull(ctx.randomness(), signature).getType()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChild(
                Signature.class, ensureNotNull(ctx.randomness(), signature).getSigFormatElement()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChild(
                Signature.class,
                ensureNotNull(ctx.randomness(), signature).getTargetFormatElement()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChild(
                Signature.class, ensureNotNull(ctx.randomness(), signature).getWhenElement()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChild(Signature.class, ensureNotNull(ctx.randomness(), signature).getWho()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChild(
                Signature.class, ensureNotNull(ctx.randomness(), signature).getOnBehalfOf()));

    mutators.add(
        (ctx, signature) ->
            ctx.fuzzChild(
                Signature.class, ensureNotNull(ctx.randomness(), signature).getDataElement()));

    return mutators;
  }

  private static Signature ensureNotNull(Randomness randomness, Signature signature) {
    if (signature == null) {
      signature = randomness.fhir().createType(Signature.class);
    }
    return signature;
  }
}
