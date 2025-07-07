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

package de.gematik.bbriccs.idp.factory;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.idp.data.Nonce;
import de.gematik.bbriccs.idp.exception.SignatureBException;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.konnektor.KonnektorImpl;
import de.gematik.bbriccs.konnektor.SofKonServicePort;
import de.gematik.bbriccs.konnektor.cfg.KonnektorContextConfiguration;
import de.gematik.bbriccs.konnektor.vsdm.VsdmService;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.val;
import org.junit.jupiter.api.Test;

class JwtFactoryTest {

  private final SmartcardArchive sca = SmartcardArchive.fromResources();

  @Test
  void shouldSignNonce() {
    val softKon = createTestKonnektor(sca);

    val smartcard = sca.getEgk(0);
    val nonce = Nonce.random();
    UnaryOperator<byte[]> signer =
        challenge -> softKon.externalAuthenticate(smartcard, challenge).getPayload();

    val signedJwt = assertDoesNotThrow(() -> JwtFactory.signJwt(smartcard, signer, nonce));
    assertNotNull(signedJwt);
    // TODO: how to verify the signed JWT?
  }

  @Test
  void shouldThrowOnInvalidChallengeSignature() {
    val smartcard = sca.getEgk(0);
    val nonce = Nonce.random();
    UnaryOperator<byte[]> signer = challenge -> challenge;
    assertThrows(SignatureBException.class, () -> JwtFactory.signJwt(smartcard, signer, nonce));
  }

  private static Konnektor createTestKonnektor(SmartcardArchive smartcardArchive) {
    val serviceProvider =
        new SofKonServicePort(smartcardArchive, VsdmService.instantiateWithTestKey());
    return new KonnektorImpl(
        KonnektorContextConfiguration.getDefaultContextType(), serviceProvider, List.of());
  }
}
