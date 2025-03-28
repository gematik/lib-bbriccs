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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HttpRequestMethodTest {

  @ParameterizedTest
  @EnumSource(
      value = HttpRequestMethod.class,
      names = {"GET", "HEAD", "OPTIONS"})
  void shouldDisallowBodyForGetAndCo(HttpRequestMethod method) {
    assertFalse(method.allowedToHaveBody());
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpRequestMethod.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"GET", "HEAD", "OPTIONS"})
  void shouldAllowBody(HttpRequestMethod method) {
    assertTrue(method.allowedToHaveBody());
  }
}
