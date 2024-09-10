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

import de.gematik.bbriccs.fhir.fuzzing.FhirTypeMutatorProvider;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Dosage;

@Getter
public class DosageMutatorProvider implements FhirTypeMutatorProvider<Dosage> {

  private final List<FuzzingMutator<Dosage>> mutators;

  public DosageMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<Dosage>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Dosage>>();
    mutators.add((ctx, dosage) -> ctx.fuzzIdElement(Dosage.class, dosage));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChildTypes(
                Dosage.class, ensureNotNull(ctx.randomness(), dosage).getExtension()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(Dosage.class, ensureNotNull(ctx.randomness(), dosage).getAsNeeded()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(
                Dosage.class,
                ensureNotNull(ctx.randomness(), dosage).getAsNeededCodeableConcept()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChildTypes(
                Dosage.class, ensureNotNull(ctx.randomness(), dosage).getAdditionalInstruction()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(
                Dosage.class,
                ensureNotNull(ctx.randomness(), dosage).getMaxDosePerAdministration()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(
                Dosage.class, ensureNotNull(ctx.randomness(), dosage).getMaxDosePerLifetime()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(
                Dosage.class, ensureNotNull(ctx.randomness(), dosage).getMaxDosePerPeriod()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(Dosage.class, ensureNotNull(ctx.randomness(), dosage).getMethod()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(
                Dosage.class,
                ensureNotNull(ctx.randomness(), dosage).getPatientInstructionElement()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(Dosage.class, ensureNotNull(ctx.randomness(), dosage).getRoute()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(
                Dosage.class, ensureNotNull(ctx.randomness(), dosage).getSequenceElement()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(Dosage.class, ensureNotNull(ctx.randomness(), dosage).getSite()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(Dosage.class, ensureNotNull(ctx.randomness(), dosage).getTextElement()));

    mutators.add(
        (ctx, dosage) ->
            ctx.fuzzChild(Dosage.class, ensureNotNull(ctx.randomness(), dosage).getTiming()));

    return mutators;
  }

  private static Dosage ensureNotNull(Randomness randomness, Dosage dosage) {
    if (dosage == null) {
      dosage = randomness.fhir().createType(Dosage.class);
    }

    return dosage;
  }
}
