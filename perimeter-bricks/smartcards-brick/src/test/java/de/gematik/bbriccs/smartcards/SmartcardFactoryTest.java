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

package de.gematik.bbriccs.smartcards;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.smartcards.exceptions.SmartcardFactoryException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.Instant;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class SmartcardFactoryTest {

  @Test
  void shouldThrowOnNonExistentImagesFile() {
    val images = Path.of("a/b/c").toFile();
    assertThrows(SmartcardFactoryException.class, () -> SmartcardFactory.fromFileSystem(images));
  }

  @SneakyThrows
  @Test
  void shouldThrowOnInvalidImagesFile() {
    val tmpDir = Path.of(System.getProperty("user.dir"), "target", "tmp", Instant.now().toString());
    assertTrue(tmpDir.toFile().mkdirs());
    val tmpFile = Path.of(tmpDir.toString(), "smartcards.json").toFile();
    try (val writer = new BufferedWriter(new FileWriter(tmpFile))) {
      writer.write("{'invalid': 'images-file'}");
    }
    assertThrows(SmartcardFactoryException.class, () -> SmartcardFactory.fromFileSystem(tmpFile));
  }

  @Test
  void shouldLoadEgkByKvnr() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    val factory = SmartcardFactory.fromFileSystem(archiveFile);
    val egkConfig = factory.getConfigFor(SmartcardType.EGK, 0);
    val egk = assertDoesNotThrow(() -> factory.loadEgkByKvnr(egkConfig.getIdentifier()));
    assertNotNull(egk);
  }
}
