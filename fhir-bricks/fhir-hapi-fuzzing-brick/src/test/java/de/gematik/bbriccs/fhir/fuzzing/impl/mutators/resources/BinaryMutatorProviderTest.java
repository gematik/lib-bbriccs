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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.fuzzing.testutils.FhirFuzzingMutatorTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BinaryMutatorProviderTest extends FhirFuzzingMutatorTest {

  @ParameterizedTest
  @MethodSource
  void shouldNotThrowAnything(File f) {
    val content = ResourceLoader.readString(f);
    val bundle = fhirCodec.decode(Bundle.class, content);
    val binary =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
            .map(resource -> (Binary) resource)
            .findFirst()
            .orElseThrow();
    val mutatorProvider = new BinaryMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, binary));

              // apply a second time to ensure re-fuzzing does also work properly
              assertDoesNotThrow(() -> m.apply(this.ctx, binary));
            });
  }

  static Stream<Arguments> shouldNotThrowAnything() {
    return ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/erp/erx/1.2.0/receiptbundle", true)
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldFuzzEmptyBinaries(Binary binary) {
    val mutatorProvider = new BinaryMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, binary));
            });
  }

  static Stream<Arguments> shouldFuzzEmptyBinaries() {
    return Stream.of(
            new Binary(),
            new Binary().setData("".getBytes()),
            new Binary().setData(null),
            new Binary().setData("hello".getBytes()),
            new Binary().setData("SGVsbG9Xb3JsZA==".getBytes()),
            new Binary().setContent("".getBytes()),
            new Binary().setContent("hello".getBytes()),
            new Binary().setContent("SGVsbG9Xb3JsZA==".getBytes()))
        .map(Arguments::of);
  }
}
