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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.fuzzing.testutils.FhirFuzzingMutatorTest;
import java.math.BigDecimal;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.IdType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class IdTypeMutatorProviderTest extends FhirFuzzingMutatorTest {

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldNotThrowAnything(IdType idType) {
    val mutatorProvider = new IdTypeMutatorProvider();

    mutatorProvider
        .getMutators()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.apply(this.ctx, idType));

              // apply a second time to ensure re-fuzzing does also work properly
              assertDoesNotThrow(() -> m.apply(this.ctx, idType));
            });
  }

  static Stream<Arguments> shouldNotThrowAnything() {
    return Stream.of(
            new IdType(),
            new IdType(new BigDecimal(123)),
            new IdType(456),
            new IdType("some random value"),
            new IdType("Medication", new BigDecimal(789)),
            new IdType("Medication", 12345L),
            new IdType("Medication", "some ID", "0.1.2"),
            new IdType("https://gematik.de/fhir/fuzzing", "Medication", "some ID", "0.1.2"))
        .map(Arguments::of);
  }
}
