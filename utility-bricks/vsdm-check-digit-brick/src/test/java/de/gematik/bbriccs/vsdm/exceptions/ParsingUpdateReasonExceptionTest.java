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

package de.gematik.bbriccs.vsdm.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class ParsingUpdateReasonExceptionTest {

  @Test
  void shouldContainInvalidCharInMessage() {
    var ex = new ParsingUpdateReasonException('X');
    assertNotNull(ex.getMessage());
    assertTrue(ex.getMessage().contains("X"));
  }

  @Test
  void shouldContainBase64DataAndPositionInMessage() {
    byte[] data = {0x01, 0x02, 0x03};
    int pos = 7;
    var ex = new ParsingUpdateReasonException(data, pos);
    assertNotNull(ex.getMessage());
    assertTrue(ex.getMessage().contains(Base64.getEncoder().encodeToString(data)));
    assertTrue(ex.getMessage().contains(String.valueOf(pos)));
  }

  @Test
  void shouldContainBase64DataAndRangeInMessage() {
    byte[] data = {0x0A, 0x0B, 0x0C, 0x0D};
    int from = 2;
    int to = 4;
    var ex = new ParsingUpdateReasonException(data, from, to);
    assertNotNull(ex.getMessage());
    assertTrue(ex.getMessage().contains(Base64.getEncoder().encodeToString(data)));
    assertTrue(ex.getMessage().contains(String.valueOf(from)));
    assertTrue(ex.getMessage().contains(String.valueOf(to)));
  }

  @Test
  void shouldBeInstanceOfRuntimeException() {
    var ex = new ParsingUpdateReasonException('A');
    assertInstanceOf(RuntimeException.class, ex);
  }
}
