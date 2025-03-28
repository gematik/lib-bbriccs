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
 */

package de.gematik.bbriccs.cli.param;

import static java.text.MessageFormat.format;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Getter
public class InputDirectoryParameter implements Iterator<File> {

  private static final List<String> ALWAYS_EXCLUDE = List.of(".DS_Store", ".gitignore");

  @Parameters(
      index = "0",
      paramLabel = "InputPath",
      description = "Path to directory where files should be read from")
  private Path in;

  @Option(
      names = {"-r", "--recursive"},
      type = Boolean.class,
      description = "Read subdirectories recursively (default=${DEFAULT-VALUE})")
  private boolean recursive = false;

  private List<Path> filePaths;

  public Path getInputPath() {
    return in;
  }

  public Path getInputDirectory() {
    if (this.in.toFile().isFile()) {
      return this.in.getParent();
    } else {
      return this.in;
    }
  }

  @Override
  public boolean hasNext() {
    this.checkInit();
    return !filePaths.isEmpty();
  }

  @Override
  public File next() throws NoSuchElementException {
    if (filePaths == null) {
      throw new NoSuchElementException(
          format("Iterator not initialized for {0} / have you called hasNext()?", in));
    } else if (filePaths.isEmpty()) {
      throw new NoSuchElementException(format("No more files in {0} left over to iterate", in));
    } else {
      return filePaths.remove(0).toFile();
    }
  }

  @SneakyThrows
  private void checkInit() {
    if (filePaths == null) {
      if (in.toFile().isDirectory()) {
        val maxDepth = recursive ? Integer.MAX_VALUE : 1;
        try (val walker = Files.find(in, maxDepth, createMatcher())) {
          this.filePaths =
              walker
                  .filter(f -> !ALWAYS_EXCLUDE.contains(f.toFile().getName()))
                  .collect(Collectors.toCollection(LinkedList::new));
        }
      } else {
        this.filePaths = new ArrayList<>(1);
        this.filePaths.add(in.toFile().toPath());
      }
    }
  }

  private BiPredicate<Path, BasicFileAttributes> createMatcher() {
    return (filePath, fileAttr) -> fileAttr.isRegularFile();
  }
}
