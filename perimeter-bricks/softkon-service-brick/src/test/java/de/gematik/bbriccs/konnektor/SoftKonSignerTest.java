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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.exceptions.SmartcardException;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardP12;
import eu.europa.esig.dss.model.DSSException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SoftKonSignerTest {

  private static final SmartcardArchive sca = SmartcardArchive.fromResources();

  static Stream<Arguments> shouldSignDocumentWithSmartcard() {
    return Stream.of(sca.getHba(0), sca.getSmcB(0)).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldSignDocumentWithSmartcard(SmartcardP12 smartcard) {
    val signer = new SoftKonSigner();
    val data = "HelloWorld".getBytes();
    val signed =
        assertDoesNotThrow(
            () -> signer.signDocument(smartcard, CryptoSystem.RSA_2048, false, data));
    assertNotNull(signed);
    assertTrue(signed.length > 0);
  }

  @ParameterizedTest
  @MethodSource("shouldSignDocumentWithSmartcard")
  void shouldThrowOnByteArrayOperation(SmartcardP12 smartcard) {
    val signer = new SoftKonSigner();
    val data = "HelloWorld".getBytes();
    try (val mio = mockStatic(IOUtils.class)) {
      mio.when(() -> IOUtils.toByteArray(any(InputStream.class))).thenThrow(new IOException());
      assertThrows(
          DSSException.class,
          () -> signer.signDocument(smartcard, CryptoSystem.RSA_2048, false, data));
    }
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldSignDocumentWithHba(CryptoSystem cryptoSystem) {
    val smartcard = sca.getHba(0);
    val signer = new SoftKonSigner();
    val signed =
        assertDoesNotThrow(() -> signer.signDocument(smartcard, cryptoSystem, false, "HelloWorld"));
    assertNotNull(signed);
    assertTrue(signed.length > 0);
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldSignDocumentWithSmcb(CryptoSystem cryptoSystem) {
    val smartcard = sca.getSmcB(0);
    val signer = new SoftKonSigner();
    val signed =
        assertDoesNotThrow(() -> signer.signDocument(smartcard, cryptoSystem, false, "HelloWorld"));
    assertNotNull(signed);
    assertTrue(signed.length > 0);
  }

  @Test
  void shouldThrowOnSigningWithInvalidSmartcard() {
    val signer = new SoftKonSigner();

    val smartcard = sca.getEgk(0);
    val data = "HelloWorld".getBytes();
    assertThrows(
        SmartcardException.class,
        () -> signer.signDocument(smartcard, CryptoSystem.RSA_2048, false, data));
  }
}
