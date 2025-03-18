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

package de.gematik.bbriccs.konnektor.vsdm;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.konnektor.exceptions.ParsingUpdateReasonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VsdmUpdateReasonTest {

  @Test
  void shouldThrowOnInvalidChecksum() {
    assertThrows(ParsingUpdateReasonException.class, () -> VsdmUpdateReason.fromChecksum('X'));
  }

  @ParameterizedTest
  @EnumSource(VsdmUpdateReason.class)
  void shouldMapFromChecksum(VsdmUpdateReason reason) {
    assertEquals(reason, VsdmUpdateReason.fromChecksum(reason.getIdentifier()));
  }

  @ParameterizedTest
  @EnumSource(VsdmUpdateReason.class)
  void shouldContainDescriptionInToString(VsdmUpdateReason reason) {
    assertTrue(reason.toString().contains(reason.getDescription()));
  }
}
