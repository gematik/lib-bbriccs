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

package de.gematik.bbriccs.konnektor.requests.options;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SignDocumentOptionsTest {

  static Stream<Arguments> shouldProperlyMapCryptoSystem() {
    return Stream.of(
        Arguments.of(CryptoSystem.RSA_2048, SigningCryptType.RSA),
        Arguments.of(CryptoSystem.RSA_PSS_2048, SigningCryptType.RSA),
        Arguments.of(CryptoSystem.ECC_256, SigningCryptType.ECC));
  }

  @Test
  void shouldBuildDefaultOptions() {
    val options = SignDocumentOptions.getDefaultOptions();
    assertTrue(options.isIncludeEContent());
    assertEquals(SignatureType.RFC_5652, options.getSignatureType());
    assertEquals("NONE", options.getTvMode());
    assertEquals("text/plain; charset=utf-8", options.getMimeType());
    assertEquals(SigningCryptType.RSA_ECC, options.getCryptoType());
  }

  @ParameterizedTest
  @MethodSource
  void shouldProperlyMapCryptoSystem(CryptoSystem input, SigningCryptType expected) {
    val rsaOptions = SignDocumentOptions.withAlgorithm(input);
    assertEquals(expected, rsaOptions.getCryptoType());
  }
}
