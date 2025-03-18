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

package de.gematik.bbriccs.fhir.builder;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.FakerException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FakerBrickTest {

  @Test
  void shouldInstantiateLongLivingFakerBrick() {
    val faker1 = new FakerBrick(new Locale("de"), new SecureRandom("seed".getBytes()));
    val faker2 = new FakerBrick(new Locale("de"), new SecureRandom("seed".getBytes()));

    // check some sub-modules from faker to ensure we have 2 dedicate Faker-objects
    assertNotEquals(faker1.app(), faker2.app());
  }

  @Test
  void shouldInstantiateShortLivingFakerBrick() {
    val faker1 = FakerBrick.getGerman();
    val faker2 = FakerBrick.getGerman();

    // check some sub-modules from faker to ensure we have a shared Faker-object
    assertEquals(faker1.app(), faker2.app());

    // but still two different FakerBricks (sharing a single underlying Faker-object)
    assertNotEquals(faker1, faker2);
  }

  @Test
  void shouldGetRandomEnumValue() {
    val faker = FakerBrick.getGerman();
    val r =
        IntStream.range(0, 100)
            .mapToObj(i -> faker.randomEnum(TestEnumeration.class))
            .distinct()
            .count();
    assertEquals(TestEnumeration.values().length, r);
  }

  @Test
  void shouldGetRandomEnumValueWithSingleExclude() {
    val faker = FakerBrick.getGerman();
    val r =
        IntStream.range(0, 100)
            .mapToObj(i -> faker.randomEnum(TestEnumeration.class, TestEnumeration.A))
            .distinct()
            .toList();
    assertEquals(TestEnumeration.values().length - 1, r.size());
    assertFalse(r.contains(TestEnumeration.A));
  }

  @Test
  void shouldGenerateRandomPrices() {
    val faker = FakerBrick.getGerman();
    val price = Assertions.assertDoesNotThrow(() -> faker.price());
    assertTrue(price >= 0);
  }

  @Test
  void shouldGetRandomEnumValueWithMultipleExclude() {
    val faker = FakerBrick.getGerman();
    val r =
        IntStream.range(0, 100)
            .mapToObj(
                i ->
                    faker.randomEnum(
                        TestEnumeration.class, List.of(TestEnumeration.A, TestEnumeration.G)))
            .distinct()
            .toList();
    assertEquals(TestEnumeration.values().length - 2, r.size());
    assertFalse(r.contains(TestEnumeration.A));
    assertFalse(r.contains(TestEnumeration.G));
  }

  @Test
  void shouldThrowIfAnythingExcluded() {
    val faker = FakerBrick.getGerman();
    val exclusion = List.of(TestEnumeration.values());
    assertThrows(FakerException.class, () -> faker.randomEnum(TestEnumeration.class, exclusion));
  }

  @Test
  void shouldGetRandomElement() {
    val faker = FakerBrick.getGerman();
    val r =
        IntStream.range(0, 100)
            .mapToObj(i -> faker.randomElement(TestEnumeration.B, TestEnumeration.C))
            .distinct()
            .count();
    assertEquals(2, r);
  }

  private enum TestEnumeration {
    A,
    B,
    C,
    D,
    E,
    F,
    G;
  }
}
