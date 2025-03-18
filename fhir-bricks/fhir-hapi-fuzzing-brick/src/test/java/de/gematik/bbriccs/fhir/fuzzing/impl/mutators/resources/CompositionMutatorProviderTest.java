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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.fuzzing.testutils.FhirFuzzingMutatorTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Getter
class CompositionMutatorProviderTest extends FhirFuzzingMutatorTest {

  @ParameterizedTest
  @MethodSource
  void shouldNotThrowAnything(File f) {
    val content = ResourceLoader.readString(f);
    val bundle = fhirCodec.decode(Bundle.class, content);
    val composition =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Composition))
            .map(resource -> (Composition) resource)
            .findFirst()
            .orElseThrow();
    val mutatorProvider = new CompositionMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, composition));

              // apply a second time to ensure re-fuzzing does also work properly
              assertDoesNotThrow(() -> m.apply(this.ctx, composition));
            });
  }

  static Stream<Arguments> shouldNotThrowAnything() {
    // choose some bundles from examples for fuzzing mutator to act on
    val ciBundles =
        ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/erp/kbv/1.1.0/bundle", true);
    val kbvBundles =
        ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/erp/kbv/1.0.2/bundle", true);

    return Stream.concat(ciBundles.stream(), kbvBundles.stream()).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldNotThrowOnEmptyComposition(Composition composition) {
    val mutatorProvider = new CompositionMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, composition));

              // apply a second time to ensure re-fuzzing does also work properly
              assertDoesNotThrow(() -> m.apply(this.ctx, composition));
            });
  }

  static Stream<Arguments> shouldNotThrowOnEmptyComposition() {
    return Stream.of(
            new Composition(),
            new Composition().setAuthor(List.of()), // this will result in an ErrorLogEntry
            new Composition().setAuthor(new ArrayList<>()),
            new Composition().setCategory(new ArrayList<>()),
            new Composition().setAttester(new ArrayList<>()))
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("shouldNotThrowOnEmptyComposition")
  void shouldNotThrowOnEmptyCompositionWithoutSections(Composition composition) {
    val mutatorProvider = new CompositionMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, composition));

              // apply a second time to ensure re-fuzzing does also work properly
              composition.setSection(new LinkedList<>());
              assertDoesNotThrow(() -> m.apply(this.ctx, composition));
            });
  }
}
