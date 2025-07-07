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

package de.gematik.bbriccs.fhir.fuzzing.impl.log;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FuzzingSessionLogbook {

  private LocalDateTime started;
  private Duration duration;
  private long mutations;
  private long added;
  private long noops;
  private RootFuzzingLogEntry sessionLog;

  public static FuzzingSessionLogbook logSession(
      String message, Duration duration, List<FuzzingLogEntry> children) {
    val parentLog = FuzzingLogEntry.parent(message, children);
    val mutations =
        children.stream()
            .filter(cfle -> cfle.getClass().equals(RootFuzzingLogEntry.class))
            .map(cfle -> ((RootFuzzingLogEntry) cfle).getMutations())
            .reduce(0L, Long::sum);
    val added =
        children.stream()
            .filter(cfle -> cfle.getClass().equals(RootFuzzingLogEntry.class))
            .map(cfle -> ((RootFuzzingLogEntry) cfle).getAdded())
            .reduce(0L, Long::sum);
    val noops =
        children.stream()
            .filter(cfle -> cfle.getClass().equals(RootFuzzingLogEntry.class))
            .map(cfle -> ((RootFuzzingLogEntry) cfle).getNoops())
            .reduce(0L, Long::sum);
    return new FuzzingSessionLogbook(
        LocalDateTime.now(), duration, mutations, added, noops, parentLog);
  }

  public long changes() {
    return mutations + added;
  }
}
