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
 */

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SKEventServicePortTypeTest {

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
    val eventService = softKonServiceProvider.getEventService();
    assertThrows(
        NotImplementedException.class, () -> eventService.subscribe(ctx, null, null, null, null));

    assertThrows(NotImplementedException.class, () -> eventService.unsubscribe(ctx, null, null));

    assertThrows(NotImplementedException.class, () -> eventService.getSubscription(null));

    assertThrows(
        NotImplementedException.class,
        () ->
            eventService.getResourceInformation(
                ctx, null, null, null, null, null, null, null, null));

    assertThrows(NotImplementedException.class, () -> eventService.getCardTerminals(null));

    assertThrows(
        NotImplementedException.class,
        () -> eventService.renewSubscriptions(ctx, null, null, null));
  }
}
