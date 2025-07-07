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

package de.gematik.bbriccs.fhir.fuzzing.impl.rnd;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.FakerException;
import de.gematik.bbriccs.fhir.fuzzing.exceptions.FuzzerException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import lombok.val;
import org.junit.jupiter.api.Test;

class RandomnessImplTest {

  private static final Random rnd = new SecureRandom();

  @Test
  void shouldThrowOnChoosingFromEmptyList() {
    val dice = new ProbabilityDiceImpl(rnd, 1.0);
    val randomness = new RandomnessImpl(rnd, dice, dice);
    val list = List.of();
    assertThrows(FuzzerException.class, () -> randomness.chooseRandomElement(list));
  }

  @Test
  void shouldPickRandomlyFromList() {
    val dice = new ProbabilityDiceImpl(rnd, 0.5);
    val randomness = new RandomnessImpl(rnd, dice, dice);
    val list = List.of(1, 2, 3);
    val element = assertDoesNotThrow(() -> randomness.chooseRandomElement(list));
    assertTrue(element > 0);
    assertTrue(element < 4);
  }

  @Test
  void shouldPickRandomEnum() {
    val dice = new ProbabilityDiceImpl(rnd, 1.0);
    val randomness = new RandomnessImpl(rnd, dice, dice);
    val e = randomness.chooseRandomFromEnum(TestEnum.class);
    assertNotNull(e);
  }

  @Test
  void shouldPickRandomEnumExclude01() {
    val dice = new ProbabilityDiceImpl(rnd, 1.0);
    val randomness = new RandomnessImpl(rnd, dice, dice);
    val e = randomness.chooseRandomFromEnum(TestEnum.class, TestEnum.A);
    assertNotNull(e);
    assertNotEquals(TestEnum.A, e);
  }

  @Test
  void shouldPickRandomEnumExclude02() {
    val dice = new ProbabilityDiceImpl(rnd, 1.0);
    val randomness = new RandomnessImpl(rnd, dice, dice);
    val e = randomness.chooseRandomFromEnum(TestEnum.class, List.of(TestEnum.A, TestEnum.B));
    assertNotNull(e);
    assertNotEquals(TestEnum.A, e);
    assertNotEquals(TestEnum.B, e);
  }

  @Test
  void shouldPickRandomEnumExclude03() {
    val dice = new ProbabilityDiceImpl(rnd, 1.0);
    val randomness = new RandomnessImpl(rnd, dice, dice);
    val exclusion = List.of(TestEnum.A, TestEnum.B, TestEnum.C);
    assertThrows(
        FakerException.class, () -> randomness.chooseRandomFromEnum(TestEnum.class, exclusion));
  }

  public enum TestEnum {
    A,
    B,
    C
  }
}
