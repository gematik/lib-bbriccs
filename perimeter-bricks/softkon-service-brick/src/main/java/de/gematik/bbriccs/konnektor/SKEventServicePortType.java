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

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardterminalinfo.v8.CardTerminalInfoType;
import de.gematik.ws.conn.connectorcommon.v5.Connector;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.*;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import jakarta.xml.ws.Holder;
import java.math.BigInteger;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;

public class SKEventServicePortType extends SoftKonServicePortType implements EventServicePortType {

  public SKEventServicePortType(SoftKonCore softKonCore) {
    super(softKonCore);
  }

  @Override
  public void subscribe(
      ContextType context,
      SubscriptionType subscription,
      Holder<Status> status,
      Holder<String> subscriptionID,
      Holder<XMLGregorianCalendar> terminationTime) {
    throw new NotImplementedException("Subscribe not implemented yet");
  }

  @Override
  public Status unsubscribe(ContextType context, String subscriptionID, String eventTo) {
    throw new NotImplementedException("Unsubscribe not implemented yet");
  }

  @Override
  public GetSubscriptionResponse getSubscription(GetSubscription parameter) {
    throw new NotImplementedException("Get Subscription not implemented yet");
  }

  @Override
  public void getResourceInformation(
      ContextType context,
      String ctId,
      BigInteger slotId,
      String iccsn,
      String cardHandle,
      Holder<Status> status,
      Holder<CardInfoType> card,
      Holder<CardTerminalInfoType> cardTerminal,
      Holder<Connector> connector) {
    throw new NotImplementedException("Resource Information not implemented yet");
  }

  @Override
  public GetCardTerminalsResponse getCardTerminals(GetCardTerminals parameter) {
    throw new NotImplementedException("Get CardTerminal not implemented yet");
  }

  @Override
  public GetCardsResponse getCards(GetCards parameter) {
    val response = new GetCardsResponse();
    response.setCards(softKonCore.getAllCards());
    return response;
  }

  @Override
  public void renewSubscriptions(
      ContextType context,
      List<String> subscriptionID,
      Holder<Status> status,
      Holder<RenewSubscriptionsResponse.SubscribeRenewals> subscribeRenewals) {
    throw new NotImplementedException("Renew Subscribe not implemented yet");
  }
}
