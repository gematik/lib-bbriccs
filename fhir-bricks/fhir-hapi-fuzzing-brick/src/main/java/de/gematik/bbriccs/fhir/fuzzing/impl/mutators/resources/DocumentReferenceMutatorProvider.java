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
import org.hl7.fhir.r4.model.DocumentReference;

public class DocumentReferenceMutatorProvider
    extends BaseDomainResourceMutatorProvider<DocumentReference> {

  public DocumentReferenceMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<DocumentReference>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<DocumentReference>>();

    mutators.add(
        (ctx, dr) ->
            ctx.fuzzChildTypes(dr.getClass(), dr.getIdentifier(), dr::getIdentifierFirstRep));

    mutators.add(
        (ctx, dr) -> ctx.fuzzChildTypes(dr.getClass(), dr.getAuthor(), dr::getAuthorFirstRep));

    mutators.add((ctx, dr) -> ctx.fuzzChild(dr, dr::hasType, dr::getType));
    mutators.add((ctx, dr) -> ctx.fuzzChild(dr, dr::hasDate, dr::getDateElement));
    mutators.add((ctx, dr) -> ctx.fuzzChild(dr, dr::hasDescription, dr::getDescriptionElement));
    mutators.add((ctx, dr) -> ctx.fuzzChild(dr, dr::hasCustodian, dr::getCustodian));
    mutators.add((ctx, dr) -> ctx.fuzzChild(dr, dr::hasAuthenticator, dr::getAuthenticator));

    return mutators;
  }
}
