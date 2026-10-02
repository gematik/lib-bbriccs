/*
 * Copyright (Change Date see Readme) gematik GmbH
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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.exceptions.SmartcardException;
import de.gematik.bbriccs.smartcards.InstituteSmartcardP12;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.stream.Stream;
import lombok.val;
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
  void shouldSignDocumentWithSmartcard(InstituteSmartcardP12 smartcard) {
    val signer = SoftKonSigner.sign(smartcard, CryptoSystem.RSA_2048);
    val data = "HelloWorld".getBytes();
    byte[] signed = assertDoesNotThrow(() -> signer.signDocument(false, data));
    assertNotNull(signed);
    assertTrue(signed.length > 0);
  }

  @ParameterizedTest
  @MethodSource("shouldSignDocumentWithSmartcard")
  void shouldThrowOnNullData(InstituteSmartcardP12 smartcard) {
    val signer = SoftKonSigner.sign(smartcard, CryptoSystem.RSA_2048);
    assertThrows(NullPointerException.class, () -> signer.signDocument(false, (byte[]) null));
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldSignDocumentWithHba(CryptoSystem cryptoSystem) {
    val smartcard = sca.getHba(0);
    val signer = SoftKonSigner.signQES(smartcard, cryptoSystem);
    byte[] signed = assertDoesNotThrow(() -> signer.signDocument(false, "HelloWorld"));
    assertNotNull(signed);
    assertTrue(signed.length > 0);
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void shouldSignDocumentWithSmcb(CryptoSystem cryptoSystem) {
    val smartcard = sca.getSmcB(0);
    val signer = SoftKonSigner.signNonQES(smartcard, cryptoSystem);
    byte[] signed = assertDoesNotThrow(() -> signer.signDocument(false, "HelloWorld"));
    assertNotNull(signed);
    assertTrue(signed.length > 0);
  }

  @Test
  void shouldThrowOnSigningWithInvalidSmartcard() {
    val smartcard = mock(InstituteSmartcardP12.class);
    assertThrows(
        SmartcardException.class, () -> SoftKonSigner.sign(smartcard, CryptoSystem.RSA_2048));
  }
}
