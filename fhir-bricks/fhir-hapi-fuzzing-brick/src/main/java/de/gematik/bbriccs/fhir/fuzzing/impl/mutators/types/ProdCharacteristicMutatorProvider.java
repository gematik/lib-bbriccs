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
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.ProdCharacteristic;

@Getter
public class ProdCharacteristicMutatorProvider
    implements FhirTypeMutatorProvider<ProdCharacteristic> {

  private final List<FuzzingMutator<ProdCharacteristic>> mutators;

  public ProdCharacteristicMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<ProdCharacteristic>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ProdCharacteristic>>();
    mutators.add((ctx, pc) -> ctx.fuzzIdElement(ProdCharacteristic.class, pc));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getExtension()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                ProdCharacteristic.class,
                ensureNotNull(ctx.randomness(), bt).getModifierExtension()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getColor()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getImage()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChildTypes(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getImprint()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChild(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getDepth()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChild(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getHeight()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChild(
                ProdCharacteristic.class,
                ensureNotNull(ctx.randomness(), bt).getExternalDiameter()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChild(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getShapeElement()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChild(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getScoring()));

    mutators.add(
        (ctx, bt) ->
            ctx.fuzzChild(
                ProdCharacteristic.class, ensureNotNull(ctx.randomness(), bt).getNominalVolume()));

    return mutators;
  }

  private static ProdCharacteristic ensureNotNull(
      Randomness randomness, ProdCharacteristic prodCharacteristic) {
    if (prodCharacteristic == null) {
      prodCharacteristic = randomness.fhir().createType(ProdCharacteristic.class);
    }

    return prodCharacteristic;
  }
}
