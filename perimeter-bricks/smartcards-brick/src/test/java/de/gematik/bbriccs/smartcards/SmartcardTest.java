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

import de.gematik.bbriccs.crypto.certificate.CertificateTypeOid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SmartcardTest {

  private static SmartcardArchive archive;

  @BeforeAll
  static void setupArchive() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    archive = SmartcardArchive.from(archiveFile);
  }

  @ParameterizedTest
  @MethodSource
  void shouldHaveOwnerData(SmartcardP12 smartcard) {
    assertDoesNotThrow(() -> smartcard.getOwnerData().getOwnerName());
    assertDoesNotThrow(smartcard::toString);
  }

  static Stream<Arguments> shouldHaveOwnerData() {
    return Stream.of(
        Arguments.of(archive.getEgk(0)),
        Arguments.of(archive.getHba(0)),
        Arguments.of(archive.getSmcB(0)));
  }

  @ParameterizedTest
  @MethodSource("shouldHaveOwnerData")
  void shouldProvideAuthCryptoKeys(SmartcardP12 smartcard) {
    assertDoesNotThrow(smartcard::getAuthPrivateKey);
    assertDoesNotThrow(smartcard::getAuthPublicKey);
  }

  @Test
  void shouldThrowOnMissingAuthCertificate() {
    List<SmartcardCertificate> certificates = List.of();
    val config = new SmartcardConfigDto();
    config.setType(SmartcardType.EGK);
    assertThrows(SmartCardKeyNotFoundException.class, () -> new EgkP12(config, certificates));
  }

  @Test
  void shouldDetectMissconfiguredType() {
    val config = new SmartcardConfigDto();
    config.setType(SmartcardType.EGK);

    assertThrows(
        InvalidSmartcardTypeException.class, () -> new TestSmartcard(SmartcardType.HBA, config));
  }

  private static class TestSmartcard extends SmartcardP12 {
    public TestSmartcard(SmartcardType type, SmartcardConfigDto config) {
      super(type, config, List.of());
    }

    @Override
    public List<CertificateTypeOid> getAutOids() {
      return List.of();
    }
  }
}
