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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RootFuzzingLogEntry extends FuzzingLogEntry {

  private long mutations;
  private long added;
  private long noops;
  private List<FuzzingLogEntry> children = new LinkedList<>();

  RootFuzzingLogEntry(String message, List<FuzzingLogEntry> children) {
    this.setType(FuzzingLogEntryType.ROOT);
    this.setMessage(message);
    this.addChildren(children);
  }

  private void addChildren(List<FuzzingLogEntry> children) {
    this.children.addAll(children);
    val directMutations =
        children.stream()
            .map(FuzzingLogEntry::getType)
            .filter(t -> t.equals(FuzzingLogEntryType.MUTATION))
            .count();
    val directAdditions =
        children.stream()
            .map(FuzzingLogEntry::getType)
            .filter(t -> t.equals(FuzzingLogEntryType.ADD))
            .count();
    val directNoops =
        children.stream()
            .map(FuzzingLogEntry::getType)
            .filter(t -> t.equals(FuzzingLogEntryType.NOOP))
            .count();

    this.setMutations(
        directMutations
            + children.stream()
                .filter(fle -> fle.getClass().equals(RootFuzzingLogEntry.class))
                .map(fle -> ((RootFuzzingLogEntry) fle).getMutations())
                .reduce(0L, Long::sum));

    this.setAdded(
        directAdditions
            + children.stream()
                .filter(fle -> fle.getClass().equals(RootFuzzingLogEntry.class))
                .map(fle -> ((RootFuzzingLogEntry) fle).getAdded())
                .reduce(0L, Long::sum));

    this.setNoops(
        directNoops
            + children.stream()
                .filter(fle -> fle.getClass().equals(RootFuzzingLogEntry.class))
                .map(fle -> ((RootFuzzingLogEntry) fle).getNoops())
                .reduce(0L, Long::sum));
  }
}
