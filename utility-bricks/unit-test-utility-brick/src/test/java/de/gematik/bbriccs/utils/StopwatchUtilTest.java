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

package de.gematik.bbriccs.utils;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class StopwatchUtilTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(StopwatchUtil.class));
  }

  @SuppressWarnings("java:S2925")
  @RepeatedTest(value = 2)
  void shouldMeasureRunnable() {
    val measurement =
        StopwatchUtil.measure(
            () -> {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            });

    assertTrue(measurement.toMillis() >= 10);
  }

  @RepeatedTest(value = 2)
  void shouldMeasureSupplier() {
    val measurement =
        StopwatchUtil.measure(() -> format("HelloWorld: {0}", System.currentTimeMillis()));
    assertTrue(measurement.response().contains("HelloWorld:"));
    assertFalse(measurement.duration().isZero());
  }
}
