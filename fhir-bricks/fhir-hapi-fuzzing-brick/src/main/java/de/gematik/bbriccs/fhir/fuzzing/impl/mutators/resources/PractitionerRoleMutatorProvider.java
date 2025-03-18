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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleMutatorProvider
    extends BaseDomainResourceMutatorProvider<PractitionerRole> {

  public PractitionerRoleMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<PractitionerRole>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<PractitionerRole>>();

    mutators.add(
        (ctx, pr) ->
            ctx.fuzzChildTypes(pr.getClass(), pr.getIdentifier(), pr::getIdentifierFirstRep));

    mutators.add(
        (ctx, pr) -> ctx.fuzzChildTypes(pr.getClass(), pr.getEndpoint(), pr::getEndpointFirstRep));

    mutators.add((ctx, pr) -> ctx.fuzzChildTypes(pr.getClass(), pr.getCode(), pr::getCodeFirstRep));

    mutators.add(
        (ctx, pr) ->
            ctx.fuzzChildTypes(pr.getClass(), pr.getSpecialty(), pr::getSpecialtyFirstRep));

    return mutators;
  }
}
