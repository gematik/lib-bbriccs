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

package de.gematik.bbriccs.konnektor.exceptions;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class ParsingUpdateReasonExceptionTest {

  @Test
  void shouldContainCharLiteralInMessage() {
    val c = 'c';
    val parsingUpdateReasonException = new ParsingUpdateReasonException(c);
    assertTrue(parsingUpdateReasonException.getMessage().contains(String.valueOf(c)));
  }

  @Test
  void shouldContainFailingPosition() {
    val position = 42;
    val parsingUpdateReasonException =
        new ParsingUpdateReasonException("content".getBytes(), position);
    assertTrue(parsingUpdateReasonException.getMessage().contains(String.valueOf(position)));
  }

  @Test
  void shouldContainFailingRange() {
    val from = 2;
    val to = 12;
    val parsingUpdateReasonException =
        new ParsingUpdateReasonException("content".getBytes(), from, to);
    assertTrue(
        parsingUpdateReasonException
            .getMessage()
            .contains(format("position {0} to {1}", from, to)));
  }
}
