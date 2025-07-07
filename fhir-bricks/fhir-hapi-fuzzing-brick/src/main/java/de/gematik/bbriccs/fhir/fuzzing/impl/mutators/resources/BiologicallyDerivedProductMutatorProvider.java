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

import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.BiologicallyDerivedProduct;

public class BiologicallyDerivedProductMutatorProvider
    extends BaseDomainResourceMutatorProvider<BiologicallyDerivedProduct> {

  public BiologicallyDerivedProductMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<BiologicallyDerivedProduct>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<BiologicallyDerivedProduct>>();

    mutators.add(
        (ctx, bdp) ->
            ctx.fuzzChildTypes(bdp.getClass(), bdp.getIdentifier(), bdp::getIdentifierFirstRep));
    mutators.add(
        (ctx, bdp) -> ctx.fuzzChildTypes(bdp.getClass(), bdp.getParent(), bdp::getParentFirstRep));
    mutators.add(
        (ctx, bdp) ->
            ctx.fuzzChildTypes(bdp.getClass(), bdp.getRequest(), bdp::getRequestFirstRep));

    mutators.add((ctx, bdp) -> ctx.fuzzChild(bdp, bdp::hasIdentifier, bdp::getIdentifierFirstRep));
    mutators.add((ctx, bdp) -> ctx.fuzzChild(bdp, bdp::hasParent, bdp::getParentFirstRep));
    mutators.add((ctx, bdp) -> ctx.fuzzChild(bdp, bdp::hasRequest, bdp::getRequestFirstRep));

    mutators.add((ctx, bdp) -> ctx.fuzzChild(bdp, bdp::hasProductCode, bdp::getProductCode));
    mutators.add((ctx, bdp) -> ctx.fuzzChild(bdp, bdp::hasQuantity, bdp::getQuantityElement));

    return mutators;
  }
}
