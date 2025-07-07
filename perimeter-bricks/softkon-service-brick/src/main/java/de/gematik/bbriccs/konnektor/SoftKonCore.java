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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.*;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import de.gematik.ws.tel.error.v2.Error;
import java.util.*;
import javax.xml.datatype.DatatypeFactory;
import lombok.SneakyThrows;
import lombok.val;

public class SoftKonCore {

  private final SmartcardArchive smartcards;
  private final SoftKonSigner signer;
  private final SoftKonVerifier verifier;
  private final CardHandleMapper cardsProvider;

  private int jobNumber = 0;

  public SoftKonCore(SmartcardArchive smartcards) {
    this.smartcards = smartcards;
    this.signer = new SoftKonSigner();
    this.verifier = new SoftKonVerifier();
    this.cardsProvider = new CardHandleMapper(smartcards);
  }

  public Cards getAllCards() {
    return this.cardsProvider.getCards();
  }

  public <T extends Smartcard> Optional<T> getSmartcardByCardHandleSafely(
      Class<T> type, String handle) {
    return Optional.ofNullable(this.cardsProvider.getCardsMap().get(handle))
        .map(cit -> this.smartcards.getByICCSN(type, cit.getIccsn()));
  }

  public <T extends Smartcard> T getSmartcardByCardHandle(Class<T> type, String handle) {
    return this.getSmartcardByCardHandleSafely(type, handle)
        .orElseThrow(
            () ->
                new CardNotFoundException(
                    format("{0} with Handle {1} not found", type.getSimpleName(), handle)));
  }

  public byte[] signDocumentWith(
      String cardHandle, CryptoSystem cryptoSystem, boolean isIncludeRevocationInfo, byte[] data)
      throws FaultMessage {
    val smartcard =
        this.getSmartcardByCardHandleSafely(SmartcardP12.class, cardHandle)
            .orElseThrow(
                () ->
                    new FaultMessage(
                        format("No card found with CardHandle {0}", cardHandle),
                        createError(cardHandle)));
    if (smartcard instanceof InstituteSmartcardP12) {
      return signer.signDocument(smartcard, cryptoSystem, isIncludeRevocationInfo, data);
    } else {
      throw new FaultMessage(
          format("Given CardHandle {0} does not belong to a institute card", cardHandle),
          createError(cardHandle));
    }
  }

  public boolean verifyDocument(byte[] data) {
    return this.verifier.verify(data);
  }

  public String getJobNumber(
      ContextType context) { // NOSONAR: I will need this parameter later on, maybe...
    return String.format("SOFT-KON-%03d", jobNumber++);
  }

  @SneakyThrows
  public Error createError(String messageId) {
    val error = new Error();
    error.setMessageID(messageId);
    error.setTimestamp(
        DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
    return error;
  }
}
