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
import org.hl7.fhir.r4.model.ContactPoint;

@Getter
public class ContactPointMutatorProvider implements FhirTypeMutatorProvider<ContactPoint> {

  private final List<FuzzingMutator<ContactPoint>> mutators;

  public ContactPointMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<ContactPoint>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ContactPoint>>();

    mutators.add((ctx, cp) -> ctx.fuzzIdElement(ContactPoint.class, cp));

    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChildTypes(
                ContactPoint.class, ensureNotNull(ctx.randomness(), cp).getExtension()));

    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChild(
                ContactPoint.class, ensureNotNull(ctx.randomness(), cp).getValueElement()));

    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChild(ContactPoint.class, ensureNotNull(ctx.randomness(), cp).getPeriod()));
    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChild(
                ContactPoint.class, ensureNotNull(ctx.randomness(), cp).getRankElement()));

    mutators.add(
        (ctx, cp) -> {
          cp = ensureNotNull(ctx.randomness(), cp);
          val ouse = cp.getUse();
          val fuse =
              ctx.randomness().chooseRandomFromEnum(ContactPoint.ContactPointUse.class, ouse);
          cp.setUse(fuse);
          return FuzzingLogEntry.operation(
              format("Change Use of ContactPoint {0}: {1} -> {2}", cp.getId(), ouse, fuse));
        });

    mutators.add(
        (ctx, cp) -> {
          cp = ensureNotNull(ctx.randomness(), cp);
          val osys = cp.getSystem();
          val fsys =
              ctx.randomness().chooseRandomFromEnum(ContactPoint.ContactPointSystem.class, osys);
          cp.setSystem(fsys);
          return FuzzingLogEntry.operation(
              format("Change System of ContactPoint {0}: {1} -> {2}", cp.getId(), osys, fsys));
        });

    return mutators;
  }

  private static ContactPoint ensureNotNull(Randomness randomness, ContactPoint cp) {
    if (cp == null) {
      cp = randomness.fhir().createType(ContactPoint.class);
    }
    return cp;
  }
}
