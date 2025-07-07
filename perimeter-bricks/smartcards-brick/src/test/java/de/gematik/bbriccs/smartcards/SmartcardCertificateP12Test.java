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

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.CertificateTypeOid;
import de.gematik.bbriccs.smartcards.exceptions.InvalidCertificateException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmartcardCertificateP12Test {

  private static SmartcardArchive archive;
  private SmartcardCertificateP12 smartcardCertificate;

  @BeforeAll
  static void setupArchive() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    archive = SmartcardArchive.from(archiveFile);
  }

  @BeforeEach
  @SneakyThrows
  void setUp() {
    val egk = archive.getEgk(0);
    val certPath =
        Paths.get(Objects.requireNonNull(this.getClass().getResource("/")).getPath(), "tmp");
    Files.createDirectories(certPath.toAbsolutePath());

    val certIS = egk.getAutCertificate().getCertificateStream();
    val certFile = Paths.get(certPath.toString(), egk.getKvnr() + ".p12");
    Files.copy(certIS.get(), certFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

    smartcardCertificate =
        new SmartcardCertificateP12(
            certFile.toString(),
            () -> {
              try {
                return Files.newInputStream(certFile);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Test
  void shouldGetX509Certificate() {
    assertNotNull(smartcardCertificate.getX509Certificate());
    assertEquals("1.2.840.10045.4.3.2", smartcardCertificate.getX509Certificate().getSigAlgOID());
  }

  @Test
  void shouldGetX509CertificateHolder() {
    assertNotNull(smartcardCertificate.getX509CertificateHolder());
    assertEquals(
        new BigInteger("1068563247561708"),
        smartcardCertificate.getX509CertificateHolder().getSerialNumber());
  }

  @Test
  void getOid() {
    assertNotNull(smartcardCertificate.getX509Certificate());
    assertEquals(CertificateTypeOid.OID_EGK_AUT, smartcardCertificate.getOid());
  }

  @Test
  void getInputStreamSupplier() {
    assertDoesNotThrow(() -> smartcardCertificate.getCertificateStream());
    assertNotNull(smartcardCertificate.getCertificateStream().get());
  }

  @Test
  void shouldThrowOnNonExistentSmartcardCertificate() {
    val tmpDir = Path.of(System.getProperty("user.dir"), "target", "tmp", Instant.now().toString());
    assertTrue(tmpDir.toFile().mkdirs());
    val tmpFile = Path.of(tmpDir.toString(), "file_0.p12").toFile();
    val tmpFilePath = tmpFile.getPath();
    val exception =
        assertThrows(
            InvalidCertificateException.class,
            () -> new SmartcardCertificateP12(tmpFilePath, null));
    assertTrue(exception.getMessage().contains(tmpFile.getName()));
  }

  @SneakyThrows
  @Test
  void shouldThrowOnInvalidSmartcardCertificate() {
    val tmpDir = Path.of(System.getProperty("user.dir"), "target", "tmp", Instant.now().toString());
    assertTrue(tmpDir.toFile().mkdirs());
    val tmpFile = Path.of(tmpDir.toString(), "file_0.p12").toFile();
    val tmpFilePath = tmpFile.getPath();
    try (val writer = new FileOutputStream(tmpFile)) {
      writer.write("Hello World".getBytes());
    }

    val exception =
        assertThrows(
            InvalidCertificateException.class,
            () -> new SmartcardCertificateP12(tmpFilePath, null));
    assertTrue(exception.getMessage().contains(tmpFile.getName()));
  }

  @Test
  void shouldGetPrivateKey() {
    assertNotNull(smartcardCertificate.getPrivateKey());
  }

  @Test
  void shouldGetAlgorithm() {
    assertEquals(CryptoSystem.ECC_256, smartcardCertificate.getCryptoSystem());
  }

  @Test
  void shouldGetDefaultStoreProtection() {
    val p = assertDoesNotThrow(() -> smartcardCertificate.getP12KeyStoreProtection());
    assertNotNull(p);
    assertEquals("00", new String(p.getPassword()));
  }
}
