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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.fuzzing.testutils.FhirFuzzingMutatorTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChargeItemDefinitionMutatorProviderTest extends FhirFuzzingMutatorTest {

  @ParameterizedTest
  @MethodSource
  void shouldNotThrowAnything(File f) {
    val content = ResourceLoader.readString(f);
    val chargeItemDefinition = fhirCodec.decode(ChargeItemDefinition.class, content);
    val mutatorProvider = new ChargeItemDefinitionMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, chargeItemDefinition));

              // apply a second time to ensure re-fuzzing does also work properly
              assertDoesNotThrow(() -> m.apply(this.ctx, chargeItemDefinition));
            });
  }

  static Stream<Arguments> shouldNotThrowAnything() {
    return ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/hl7/chargeitemdefinition", true)
        .stream()
        .map(Arguments::of);
  }
}