/*
 * Copyright (Change Date see Readme) gematik GmbH
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

package de.gematik.bbriccs.idp.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class GrantTypeTest {

  @ParameterizedTest
  @EnumSource(GrantType.class)
  void shouldRoundTripFromValue(GrantType grantType) {
    assertEquals(grantType, GrantType.from(grantType.value()).orElseThrow());
  }

  @Test
  void shouldHaveUniqueValues() {
    assertEquals(
        GrantType.values().length,
        Arrays.stream(GrantType.values()).map(GrantType::value).collect(Collectors.toSet()).size());
  }

  @Test
  void shouldReturnEmptyForUnknownValue() {
    assertTrue(GrantType.from("implicit").isEmpty());
  }
}
