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

package de.gematik.bbriccs.utils;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Generated;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ResourceLoader {

  private static final ClassLoader CLASS_LOADER = ResourceLoader.class.getClassLoader();

  private ResourceLoader() {
    throw new IllegalAccessError("Utility class: don't use the constructor");
  }

  /**
   * Get a file from the resources folder and returns the file as an InputStream
   *
   * @param fileName of the File to read
   * @return InputStream of the file content
   */
  public static InputStream getFileFromResourceAsStream(String fileName) {
    // The class loader that loaded the class
    val inputStream = CLASS_LOADER.getResourceAsStream(fileName);

    // the stream holding the file content
    if (inputStream == null) {
      throw new ResourceFileException(format("File {0} not found in resources!", fileName));
    } else {
      return inputStream;
    }
  }

  /**
   * Reads a file from the resources folder and returns the content as an UTF-8 encoded String
   *
   * @param fileName of the File to read
   * @return the file content as a UTF-8 encoded String
   */
  @SneakyThrows
  public static String readFileFromResource(String fileName) {
    val stream = ResourceLoader.getFileFromResourceAsStream(fileName);
    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
  }

  public static File getFileFromResource(String fileName) {
    return Optional.ofNullable(CLASS_LOADER.getResource(fileName))
        .map(URL::getFile)
        .map(path -> Path.of(path).toFile())
        .orElseThrow(
            () -> new ResourceFileException(format("File {0} not found in resources", fileName)));
  }

  public static List<String> readFilesFromDirectory(String path) {
    return readFilesFromDirectory(path, false);
  }

  public static List<String> readFilesFromDirectory(String path, boolean recursive) {
    return getResourceFilesInDirectory(path, recursive).stream()
        .map(ResourceLoader::readString)
        .toList();
  }

  public static List<File> getResourceFilesInDirectory(String path) {
    return getResourceFilesInDirectory(path, false);
  }

  public static List<File> getResourceFilesInDirectory(String path, boolean recursive) {
    val dir = toExistingResourceDirectoryPath(path);
    val maxDepth = recursive ? Integer.MAX_VALUE : 1;
    try (val walker = getFileWalker(dir, maxDepth)) {
      return walker.map(Path::toFile).toList();
    }
  }

  /**
   * Outsource and annotate with @SneakyThrows and @Generated to prevent sonar from checking
   * coverage for this one
   *
   * @param path to walk from
   * @param maxDepth to walk
   * @return stream a files in the given path
   */
  @SneakyThrows
  @Generated
  private static Stream<Path> getFileWalker(Path path, int maxDepth) {
    return Files.find(path, maxDepth, (filePath, fileAttr) -> fileAttr.isRegularFile());
  }

  public static List<File> getResourceDirectoryStructure(String path) {
    return getResourceDirectoryStructure(path, true);
  }

  @SneakyThrows
  public static List<File> getResourceDirectoryStructure(String path, boolean recursive) {
    val dir = toExistingResourceDirectoryPath(path);

    val maxDepth = recursive ? Integer.MAX_VALUE : 1;
    try (val walker =
        Files.find(
            dir,
            maxDepth,
            (filePath, fileAttr) -> fileAttr.isRegularFile() || fileAttr.isDirectory())) {
      return walker
          .filter(p -> !p.equals(dir)) // filter to root directory!
          .map(Path::toFile)
          .toList();
    }
  }

  public static String readString(File file) {
    try {
      return Files.readString(Path.of(file.getAbsolutePath()));
    } catch (IOException e) {
      throw new ResourceFileException(
          format("Error while reading from file {0}", file.getAbsolutePath()));
    }
  }

  @SneakyThrows
  private static Path toExistingResourceDirectoryPath(String path) {
    val pathUrl =
        Objects.requireNonNull(
                CLASS_LOADER.getResource(path), format("Unable to read file from {0}", path))
            .toURI();
    val dir = Path.of(pathUrl);

    val file = dir.toFile();
    if (!file.exists() || !file.isDirectory()) {
      throw new ResourceFileException(
          format("Given path {0} does not exist or is not a directory", path));
    }

    return dir;
  }
}
