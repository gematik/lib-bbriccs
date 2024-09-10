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

package de.gematik.bbriccs.fhir.fuzzing.impl.log;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class FuzzingSessionLogbookTest {

  @Test
  void shouldStarEmptySessionLogbook() {
    val fslb = FuzzingSessionLogbook.logSession("Test Session", Duration.ZERO, List.of());
    assertEquals(1, fslb.getSessionLog().getChildren().size());

    val first = fslb.getSessionLog().getChildren().get(0);
    assertEquals(NoOpFuzzingLogEntry.class, first.getClass());
  }
}
