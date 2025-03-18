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

package de.gematik.bbriccs.cli.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FileWalkerTest {

  @SneakyThrows
  private Path createWalkPath(int files, int directories) {
    val walkDir =
        Path.of(System.getProperty("user.dir"), "target", "tmp", Instant.now().toString());
    assertTrue(walkDir.toFile().mkdirs());

    for (var i = 0; i < files; i++) {
      val f = Path.of(walkDir.toString(), "file_" + i + ".txt").toFile();
      try (val writer = new BufferedWriter(new FileWriter(f))) {
        writer.write(i);
      }
    }

    for (var i = 0; i < directories; i++) {
      val dir = Path.of(walkDir.toString(), "dir_" + i).toFile();
      assertTrue(dir.mkdirs());
    }

    return walkDir;
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldWalkDirectoryFiles(boolean recursive) {
    val walkDir = createWalkPath(3, 2);

    val walker = new FileWalker(walkDir, recursive);
    val files = walker.find(FileWalker.WalkType.FILES);
    assertEquals(3, files.size());
  }

  @Test
  void shouldWalkDirectory() {
    val walkDir = createWalkPath(2, 3);

    val walker = new FileWalker(walkDir);
    val files = walker.find(FileWalker.WalkType.DIRECTORIES);
    assertEquals(3, files.size());
  }

  @Test
  void shouldNotFailWhenStartingWithFile() {
    val walkDirBase = createWalkPath(2, 3);
    val walkDir = Path.of(walkDirBase.toString(), "file_0.txt");

    val walker = new FileWalker(walkDir);
    val files = walker.find(FileWalker.WalkType.DIRECTORIES);
    assertEquals(1, files.size());
  }

  @Test
  void shouldThrowOnInvalidPath() {
    val walker = new FileWalker(Path.of("/a/b/c"));
    assertThrows(IOException.class, () -> walker.find(FileWalker.WalkType.FILES));
  }
}
