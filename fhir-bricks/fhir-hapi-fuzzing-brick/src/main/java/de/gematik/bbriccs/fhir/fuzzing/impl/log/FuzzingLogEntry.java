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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;
import lombok.val;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class FuzzingLogEntry {

  private FuzzingLogEntryType type;
  private String message;

  public static RootFuzzingLogEntry parent(String message, FuzzingLogEntry child) {
    return parent(message, List.of(child));
  }

  public static RootFuzzingLogEntry parent(String message, List<FuzzingLogEntry> children) {
    if (children.isEmpty()) {
      return FuzzingLogEntry.parent(message, FuzzingLogEntry.noop("no children"));
    } else {
      return new RootFuzzingLogEntry(message, children);
    }
  }

  public static RootFuzzingLogEntry add(String message, FuzzingLogEntry child) {
    val entry = new RootFuzzingLogEntry(message, List.of(child));
    entry.setType(FuzzingLogEntryType.ADD);
    return entry;
  }

  public static FuzzingLogEntry operation(String message) {
    return new MutationFuzzingLogEntry(message);
  }

  public static FuzzingLogEntry noop(String message) {
    return new NoOpFuzzingLogEntry("No Operation: " + message);
  }

  public static FuzzingLogEntry error(Throwable throwable) {
    return new ErrorLogEntry(throwable);
  }
}
