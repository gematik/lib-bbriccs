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
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;

@Getter
public class CodeableConceptTypeMutatorProvider
    implements FhirTypeMutatorProvider<CodeableConcept> {

  private final List<FuzzingMutator<CodeableConcept>> mutators;

  public CodeableConceptTypeMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<CodeableConcept>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<CodeableConcept>>();
    mutators.add((ctx, cc) -> ctx.fuzzIdElement(CodeableConcept.class, cc));

    mutators.add(
        (ctx, cc) ->
            ctx.fuzzChildTypes(
                CodeableConcept.class, ensureNotNull(ctx.randomness(), cc).getCoding()));
    mutators.add(
        (ctx, cc) ->
            ctx.fuzzChildTypes(
                CodeableConcept.class, ensureNotNull(ctx.randomness(), cc).getExtension()));
    mutators.add(
        (ctx, cc) ->
            ctx.fuzzChild(
                CodeableConcept.class, ensureNotNull(ctx.randomness(), cc).getTextElement()));

    mutators.add(
        (ctx, cc) -> {
          cc = ensureNotNull(ctx.randomness(), cc);
          cc.setText(ctx.randomness().regexify("[A-Za-z0-9._%$+-]{1,10}"));
          return FuzzingLogEntry.operation(
              format("Set random Text to CodeableConcept: {0}", cc.getText()));
        });

    mutators.add(
        (ctx, cc) -> {
          cc = ensureNotNull(ctx.randomness(), cc);
          val coding = cc.addCoding();
          val codingFuzz = ctx.fuzzChild(CodeableConcept.class, coding);
          return FuzzingLogEntry.parent(
              format("Add random coding to CodeableConcept {0}", cc.getId()), codingFuzz);
        });

    mutators.add(
        (ctx, cc) -> {
          cc = ensureNotNull(ctx.randomness(), cc);
          val textElement = cc.getTextElement();
          val textElementFuzz = ctx.fuzzChild(CodeableConcept.class, textElement);
          return FuzzingLogEntry.parent(
              format("Add random text element to CodeableConcept {0}", cc.getId()),
              textElementFuzz);
        });

    return mutators;
  }

  private static CodeableConcept ensureNotNull(Randomness randomness, CodeableConcept cc) {
    if (cc == null) {
      cc = randomness.fhir().createType(CodeableConcept.class);
    }
    return cc;
  }
}
