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

package de.gematik.bbriccs.konnektor.requests;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import de.gematik.ws.conn.eventservice.v7.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class GetCardsRequest extends AbstractKonnektorRequest<GetCardsResponse> {

  private final boolean mandantWide;

  public GetCardsRequest() {
    this(false);
  }

  public GetCardsRequest(boolean mandantWide) {
    this.mandantWide = mandantWide;
  }

  @Override
  public GetCardsResponse execute(ContextType ctx, ServicePort serviceProvider) {
    val factory = new ObjectFactory();
    val servicePort = serviceProvider.getEventService();
    val payload = factory.createGetCards();
    payload.setMandantWide(mandantWide);
    payload.setContext(ctx);

    log.trace(format("Get cards mandantWide={0}", mandantWide));
    return this.executeSupplier(() -> servicePort.getCards(payload));
  }
}