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

package de.gematik.bbriccs.konnektor.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class InvalidKeyLengthExceptionTest {

  @Test
  void shouldStateExpectation() {
    val key = "key".getBytes();
    val exception =
        assertThrows(
            InvalidKeyLengthException.class,
            () -> {
              throw new InvalidKeyLengthException(key, 32);
            });
    assertTrue(exception.getMessage().contains("have to be 32"));
    assertTrue(exception.getMessage().contains("Key length 3 is invalid."));
  }
}
