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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.bbriccs.cfg.dto.TLSConfiguration;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrustProviderTest {

  static Stream<Arguments> shouldBuildFromConfig() {
    return Stream.of(
        arguments("resources/tls/keystore.p12", "resource/tls/truststore.p12"),
        arguments("resources/tls/keystore.p12", "resource/tls/konsim_truststore.jks"));
  }

  @ParameterizedTest
  @MethodSource
  void shouldBuildFromConfig(String keyStorePath, String trustStorePath) {
    val cfg = new TLSConfiguration();
    cfg.setKeyStore(keyStorePath);
    cfg.setKeyStorePassword("changeit");

    cfg.setTrustStore(trustStorePath);
    cfg.setTrustStorePassword("123456");

    val trustProvider = assertDoesNotThrow(() -> TrustProvider.from(cfg));
    assertNotNull(trustProvider.getSocketFactory());
  }
}
