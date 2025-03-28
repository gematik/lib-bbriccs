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
import org.hl7.fhir.r4.model.ContactDetail;

@Getter
public class ContactDetailMutatorProvider implements FhirTypeMutatorProvider<ContactDetail> {

  private final List<FuzzingMutator<ContactDetail>> mutators;

  public ContactDetailMutatorProvider() {
    this.mutators = createMutators();
  }

  private static List<FuzzingMutator<ContactDetail>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<ContactDetail>>();
    mutators.add((ctx, cp) -> ctx.fuzzIdElement(ContactDetail.class, cp));

    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChildTypes(
                ContactDetail.class, ensureNotNull(ctx.randomness(), cp).getExtension()));

    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChild(
                ContactDetail.class, ensureNotNull(ctx.randomness(), cp).getNameElement()));

    mutators.add(
        (ctx, cp) ->
            ctx.fuzzChildTypes(
                ContactDetail.class, ensureNotNull(ctx.randomness(), cp).getTelecom()));

    return mutators;
  }

  private static ContactDetail ensureNotNull(Randomness randomness, ContactDetail cd) {
    if (cd == null) {
      cd = randomness.fhir().createType(ContactDetail.class);
    }
    return cd;
  }
}
