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

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import jakarta.xml.ws.Holder;
import java.math.BigInteger;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SKCardServicePortTypeTest {
  private static ContextType ctx;
  private static SofKonServicePort softKonServiceProvider;

  @BeforeAll
  static void setup() {
    val smartCardArchive = SmartcardArchive.fromResources();
    softKonServiceProvider = new SofKonServicePort(smartCardArchive);

    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");
  }

  @Test
  void shouldThrowExceptions() {
    val cardService = softKonServiceProvider.getCardService();
    assertThrows(
        NotImplementedException.class,
        () -> cardService.changePin(ctx, null, null, null, null, null));

    assertThrows(
        NotImplementedException.class,
        () -> cardService.disablePin(ctx, null, null, null, null, null));

    assertThrows(
        NotImplementedException.class,
        () -> cardService.enablePin(ctx, null, null, null, null, null));

    assertThrows(
        NotImplementedException.class,
        () -> cardService.changePin(ctx, null, null, null, null, null));

    assertThrows(
        NotImplementedException.class,
        () -> cardService.getPinStatus(ctx, null, null, null, null, null));

    assertThrows(
        NotImplementedException.class,
        () -> cardService.unblockPin(ctx, null, null, null, null, null, null));
  }

  @Test
  void shouldAlwaysVerifyPin() {
    val cardService = softKonServiceProvider.getCardService();
    val status = new Holder<Status>();
    val pinResult = new Holder<PinResultEnum>();
    val leftTries = new Holder<BigInteger>();
    assertDoesNotThrow(() -> cardService.verifyPin(ctx, null, null, status, pinResult, leftTries));
    assertEquals("OK", status.value.getResult());
    assertEquals(PinResultEnum.OK, pinResult.value);
  }
}
