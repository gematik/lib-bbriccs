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

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import lombok.val;

public class StopwatchUtil {

  private StopwatchUtil() {
    throw new IllegalAccessError("utility class");
  }

  public static Duration measure(Runnable runnable) {
    val start = Instant.now();
    runnable.run();
    val finish = Instant.now();
    return Duration.between(start, finish);
  }

  public static <T> Measurement<T> measure(Supplier<T> runnable) {
    val start = Instant.now();
    val response = runnable.get();
    val finish = Instant.now();
    return Measurement.of(Duration.between(start, finish), response);
  }

  public record Measurement<T>(Duration duration, T response) {
    public static <T> Measurement<T> of(Duration duration, T response) {
      return new Measurement<>(duration, response);
    }
  }
}
