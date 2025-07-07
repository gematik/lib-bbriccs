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

package de.gematik.bbriccs.smartcards;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.smartcards.SmartcardOwnerData.Builder;
import de.gematik.bbriccs.smartcards.exceptions.SmartcardFactoryException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SmartcardOwnerDataTest {

  @Test
  void shouldComposeOwnerNameWithoutTitle() {
    val ownerData = SmartcardOwnerData.builder().surname("Max").givenName("Mustermann").build();
    assertEquals("Mustermann Max", ownerData.getCommonName());
    assertEquals("Mustermann, Max", ownerData.getOwnerName());
    assertEquals("Mustermann, Max", ownerData.toString());
  }

  @Test
  void shouldComposeOwnerNameWithTitle() {
    val ownerData =
        SmartcardOwnerData.builder().title("Dr.").surname("Max").givenName("Mustermann").build();
    assertEquals("Mustermann Max", ownerData.getCommonName());
    assertEquals("Dr. Mustermann, Max", ownerData.getOwnerName());
    assertEquals("Dr. Mustermann, Max", ownerData.toString());
  }

  @Test
  void shouldExtractNamesFromCommonName() {
    val ownerData = SmartcardOwnerData.builder().commonName("Max Mustermann").build();
    assertEquals("Max", ownerData.getGivenName());
    assertEquals("Mustermann", ownerData.getSurname());
  }

  @Test
  void shouldExtractNamesFromCommonNameWithoutMiddleNames() {
    val ownerData = SmartcardOwnerData.builder().commonName("Max von und zu Mustermann").build();
    assertEquals("Max", ownerData.getGivenName());
    assertEquals("Mustermann", ownerData.getSurname());
  }

  @Test
  void shouldExtractGivenNameFromCommonName() {
    val ownerData =
        SmartcardOwnerData.builder().commonName("Max Mustermann").givenName("Maximilian").build();
    assertEquals("Maximilian", ownerData.getGivenName());
    assertEquals("Mustermann", ownerData.getSurname());
  }

  @Test
  void shouldExtractSurnameFromCommonName() {
    val ownerData =
        SmartcardOwnerData.builder().commonName("Max Mustermann").surname("Müller").build();
    assertEquals("Max", ownerData.getGivenName());
    assertEquals("Müller", ownerData.getSurname());
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowOnInsufficientOwnerNameData(Consumer<Builder> builderConsumer) {
    val odb = SmartcardOwnerData.builder();
    builderConsumer.accept(odb);
    assertThrows(SmartcardFactoryException.class, odb::build);
  }

  static Stream<Arguments> shouldThrowOnInsufficientOwnerNameData() {
    return Stream.of(
        Arguments.of((Consumer<Builder>) b -> b.givenName("Max")),
        Arguments.of((Consumer<Builder>) b -> b.surname("Mustermann")));
  }
}
