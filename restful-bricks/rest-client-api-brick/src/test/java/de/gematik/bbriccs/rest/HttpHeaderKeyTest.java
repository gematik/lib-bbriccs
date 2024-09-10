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

package de.gematik.bbriccs.rest;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpHeaderKeyTest {

  @ParameterizedTest
  @MethodSource
  void shouldHaveValidHeaderName(HttpHeaderKey headerKey) {
    val header = headerKey.createHeader("ABC");
    assertNotNull(header);
    assertEquals("ABC", header.value());
    assertEquals(headerKey.getKey(), header.key());
  }

  static Stream<Arguments> shouldHaveValidHeaderName() {
    return Stream.concat(
            Stream.of(AuthHttpHeaderKey.values()), Stream.of(StandardHttpHeaderKey.values()))
        .map(Arguments::of);
  }
}
