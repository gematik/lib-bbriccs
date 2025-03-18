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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FuzzingContext;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingMutator;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.UriType;

public class BundleMutatorProvider extends BaseResourceMutatorProvider<Bundle> {

  public BundleMutatorProvider() {
    super(createMutators());
  }

  private static List<FuzzingMutator<Bundle>> createMutators() {
    val mutators = new LinkedList<FuzzingMutator<Bundle>>();

    mutators.add(FuzzingContext::fuzzChildResources);
    mutators.add(
        (ctx, bundle) ->
            ctx.fuzzChild(bundle.getClass(), bundle::hasIdentifier, bundle::getIdentifier));

    mutators.add(
        (ctx, bundle) -> {
          val uriTypes =
              bundle.getEntry().stream()
                  .map(Bundle.BundleEntryComponent::getFullUrlElement)
                  .toList();
          return ctx.fuzzChildTypes(
              bundle.getClass(), uriTypes, () -> ctx.randomness().fhir().createType(UriType.class));
        });

    mutators.add(
        (ctx, bundle) -> {
          val amount = ctx.randomness().source().nextInt(1, 3);
          val children =
              IntStream.range(0, amount)
                  .mapToObj(
                      idx -> {
                        val resource = ctx.randomness().fhir().createResource();
                        resource.setId(ctx.randomness().uuid());
                        resource.setMeta(new Meta().addProfile(ctx.randomness().url()));
                        val entry = new Bundle.BundleEntryComponent().setResource(resource);
                        bundle.addEntry(entry);

                        if (ctx.randomness().source().nextBoolean()) {
                          return ctx.fuzzChild(bundle.getClass(), resource);
                        } else {
                          return ctx.fuzzChild(resource, resource::hasMeta, resource::getMeta);
                        }
                      })
                  .toList();
          return FuzzingLogEntry.parent(
              format(
                  "Randomly add {0} BundleEntryComponents to Bundle {1}", amount, bundle.getId()),
              children);
        });

    return mutators;
  }
}
