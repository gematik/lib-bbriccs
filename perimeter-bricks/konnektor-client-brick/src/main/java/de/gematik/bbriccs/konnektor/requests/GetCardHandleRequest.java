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

import static java.text.MessageFormat.*;

import de.gematik.bbriccs.cardterminal.CardInfo;
import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.bbriccs.konnektor.exceptions.SmartcardMissmatchException;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.ws.conn.connectorcontext.v2.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
public class GetCardHandleRequest extends AbstractKonnektorRequest<CardInfo> {

  private final String iccsn;

  private GetCardHandleRequest(String iccsn) {
    this.iccsn = iccsn;
  }

  @Override
  public CardInfo execute(ContextType ctx, ServicePort serviceProvider) {
    log.trace(format("Get CardHandle for ICCSN {0}", iccsn));
    val cmd = new GetCardsRequest();
    val cardsResponse = cmd.execute(ctx, serviceProvider);

    return cardsResponse.getCards().getCard().stream()
        .filter(cit -> cit.getIccsn().equals(this.iccsn))
        .map(CardInfo::fromCardInfoType)
        .findFirst()
        .orElseThrow(
            () -> new SmartcardMissmatchException(this.getClass(), iccsn, serviceProvider));
  }

  public static GetCardHandleRequest forIccsn(String iccsn) {
    return new GetCardHandleRequest(iccsn);
  }

  public static GetCardHandleRequest forSmartcard(Smartcard smartcard) {
    return forIccsn(smartcard.getIccsn());
  }
}
