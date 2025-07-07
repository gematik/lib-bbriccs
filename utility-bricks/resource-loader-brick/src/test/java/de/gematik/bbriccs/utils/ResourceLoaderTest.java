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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.val;
import org.junit.jupiter.api.Test;

class ResourceLoaderTest {

  @Test
  void shouldNotHaveCallableConstructor() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ResourceLoader.class));
  }

  @Test
  void shouldReadSingleFile() {
    val content = ResourceLoader.readFileFromResource("txt/first.txt");
    assertEquals("First", content);
  }

  @Test
  void shouldGetFileFromResource() {
    val file = ResourceLoader.getFileFromResource("txt/first.txt");
    assertEquals("first.txt", file.getName());
  }

  @Test
  void shouldThrowOnGettingMissingFile() {
    val exception =
        assertThrows(
            ResourceFileException.class,
            () -> ResourceLoader.getFileFromResource("txt/test_123.txt"));
    assertTrue(exception.getMessage().contains("txt/test_123.txt"));
  }

  @Test
  void shouldThrowOnMissingFile() {
    val exception =
        assertThrows(
            ResourceFileException.class,
            () -> ResourceLoader.readFileFromResource("txt/test_123.txt"));
    assertTrue(exception.getMessage().contains("txt/test_123.txt not found in resources"));
  }

  @Test
  void shouldReadAllFilesFromDirectory() {
    val fileContents = assertDoesNotThrow(() -> ResourceLoader.readFilesFromDirectory("txt"));
    assertEquals(2, fileContents.size());
    assertTrue(fileContents.contains("First"));
    assertTrue(fileContents.contains("Second"));
  }

  @Test
  void shouldGetAllFilesFromDirectory() {
    val files = assertDoesNotThrow(() -> ResourceLoader.getResourceFilesInDirectory("txt"));
    assertEquals(2, files.size());

    val fileNames = files.stream().map(File::getName).toList();
    assertTrue(fileNames.contains("first.txt"));
    assertTrue(fileNames.contains("second.txt"));
  }

  @Test
  void shouldReadAllFilesFromDirectoryRecursively() {
    val fileContents = assertDoesNotThrow(() -> ResourceLoader.readFilesFromDirectory("txt", true));
    assertEquals(4, fileContents.size());
    assertTrue(fileContents.contains("Third"));
    assertTrue(fileContents.contains("Fourth"));
  }

  @Test
  void shouldGetAllFilesFromDirectoryRecursively() {
    val files = assertDoesNotThrow(() -> ResourceLoader.getResourceFilesInDirectory("txt", true));
    assertEquals(4, files.size());

    val fileNames = files.stream().map(File::getName).toList();
    assertTrue(fileNames.contains("third.txt"));
    assertTrue(fileNames.contains("fourth.txt"));
  }

  @Test
  void shouldGetCompleteDirectoryStructure() {
    val dirStructure = ResourceLoader.getResourceDirectoryStructure("txt");
    assertEquals(5, dirStructure.size());

    val dirs = dirStructure.stream().filter(File::isDirectory).toList();
    assertEquals(1, dirs.size());

    val files = dirStructure.stream().filter(File::isFile).toList();
    assertEquals(4, files.size());
  }

  @Test
  void shouldGetCompleteDirectoryStructureOnLeaf() {
    val dirStructure = ResourceLoader.getResourceDirectoryStructure("txt/subdir");
    assertEquals(2, dirStructure.size());

    val dirs = dirStructure.stream().filter(File::isDirectory).toList();
    assertEquals(0, dirs.size());

    val files = dirStructure.stream().filter(File::isFile).toList();
    assertEquals(2, files.size());
  }

  @Test
  void shouldReadEmptyDirectoryWithoutThrowing() throws IOException {
    // create empty directory dynamically because git does not track empty directories
    val basePath = Paths.get(this.getClass().getResource("/").getPath());
    val emptyDir = basePath.resolve("empty").toFile();
    Files.createDirectories(emptyDir.toPath());

    val dirStructure = ResourceLoader.getResourceDirectoryStructure("empty");
    assertEquals(0, dirStructure.size());

    val dirs = dirStructure.stream().filter(File::isDirectory).toList();
    assertEquals(0, dirs.size());

    val files = dirStructure.stream().filter(File::isFile).toList();
    assertEquals(0, files.size());
  }

  @Test
  void shouldThrowOnInvalidFileRead() {
    val f = Path.of("a/b/c").toFile();
    val exception = assertThrows(ResourceFileException.class, () -> ResourceLoader.readString(f));
    assertTrue(exception.getMessage().contains("Error while reading from file"));
    assertTrue(exception.getMessage().contains("a/b/c"));
  }
}
