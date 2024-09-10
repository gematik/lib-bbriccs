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

package de.gematik.bbriccs.cli.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class FileWalker {

  private final Path start;
  private final boolean recursive;

  public FileWalker(Path start) {
    this(start, false);
  }

  @SneakyThrows
  public List<Path> find(WalkType type) {

    if (!this.start.toFile().exists()) {
      throw new IOException("Cannot read from " + this.start.toFile().getAbsolutePath());
    }

    if (this.start.toFile().isDirectory()) {
      val maxDepth = recursive ? Integer.MAX_VALUE : 1;
      try (val walker = Files.find(this.start, maxDepth, createMatcher(type))) {
        return walker
            .filter(p -> !p.equals(this.start))
            .collect(Collectors.toCollection(LinkedList::new));
      }
    } else {
      return List.of(this.start.toFile().toPath());
    }
  }

  private BiPredicate<Path, BasicFileAttributes> createMatcher(WalkType type) {
    if (type.equals(WalkType.FILES)) {
      return (filePath, fileAttr) -> fileAttr.isRegularFile();
    } else {
      return (filePath, fileAttr) -> fileAttr.isDirectory();
    }
  }

  public enum WalkType {
    FILES,
    DIRECTORIES
  }
}
