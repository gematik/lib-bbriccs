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

package de.gematik.bbriccs.fhir.fuzzing.impl.rnd;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProbabilityDiceTest {

  private static final Random rnd = new SecureRandom();

  @ParameterizedTest
  @ValueSource(doubles = {-1.0, -0.1, 1.1, 10.0})
  void shouldThrowOnInvalidProbability(double probability) {
    assertThrows(IllegalArgumentException.class, () -> new ProbabilityDiceImpl(rnd, probability));
  }

  @ParameterizedTest
  @MethodSource
  void shouldAlwaysTossTheSameOnEdgeProbabilities(double probability, boolean expectation) {
    val dice = new ProbabilityDiceImpl(rnd, probability);
    for (var i = 0; i < 10; i++) {
      assertEquals(expectation, dice.toss());
    }
  }

  static Stream<Arguments> shouldAlwaysTossTheSameOnEdgeProbabilities() {
    return Stream.of(Arguments.of(1.0, true), Arguments.of(0.0, false));
  }
}
