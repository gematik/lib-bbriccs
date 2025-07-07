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

package de.gematik.bbriccs.fhir.fuzzing.impl.mutators.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.fuzzing.testutils.FhirFuzzingMutatorTest;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class CodeTypeMutatorProviderTest extends FhirFuzzingMutatorTest {

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldNotThrowAnything(CodeType codeType) {
    val mutatorProvider = new CodeTypeMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, codeType));

              // apply a second time to ensure re-fuzzing does also work properly
              assertDoesNotThrow(() -> m.apply(this.ctx, codeType));
            });
  }

  static Stream<Arguments> shouldNotThrowAnything() {
    return Stream.of(
            new CodeType(),
            new CodeType("ABC"),
            new CodeType("XYZ").setSystem("https://gematik.de/fhir/fuzzing"),
            new CodeType("XYZ").setExtension(List.of(new Extension())))
        .map(Arguments::of);
  }
}
