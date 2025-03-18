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

package de.gematik.bbriccs.smartcards.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.smartcards.EgkP12;
import lombok.val;
import org.junit.jupiter.api.Test;

class CardNotFoundExceptionTest {

  @Test
  void shouldNameIccsnInException() {
    val exception =
        assertThrows(
            CardNotFoundException.class,
            () -> {
              throw new CardNotFoundException("80276001011699901102");
            });
    assertTrue(exception.getMessage().contains("80276001011699901102"));
  }

  @Test
  void shouldNameTypeAndIccsnInException() {
    val exception =
        assertThrows(
            CardNotFoundException.class,
            () -> {
              throw new CardNotFoundException(EgkP12.class, "80276001011699901102");
            });
    assertTrue(exception.getMessage().contains("80276001011699901102"));
    assertTrue(exception.getMessage().contains(EgkP12.class.getSimpleName()));
  }
}
