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

package de.gematik.bbriccs.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class HttpVersionTest {

  @ParameterizedTest
  @MethodSource
  void shouldParseHttpVersionFromString(String input, HttpVersion expected) {
    val actualVersion = HttpVersion.fromString(input);
    assertEquals(expected, actualVersion);
  }

  static Stream<Arguments> shouldParseHttpVersionFromString() {
    return Stream.of(
        Arguments.of("HTTP/1.1", HttpVersion.HTTP_1_1), Arguments.of("HTTP/2", HttpVersion.HTTP_2));
  }

  @ParameterizedTest
  @ValueSource(strings = {"HTTP/3", "HTTP/1", "http"})
  @NullSource
  void shouldThrowOnInvalidHttpVersion(String input) {
    assertThrows(IllegalArgumentException.class, () -> HttpVersion.fromString(input));
  }
}
