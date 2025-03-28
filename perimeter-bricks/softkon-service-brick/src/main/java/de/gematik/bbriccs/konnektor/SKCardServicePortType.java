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

import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import jakarta.xml.ws.Holder;
import java.math.BigInteger;
import org.apache.commons.lang3.NotImplementedException;

public class SKCardServicePortType extends SoftKonServicePortType implements CardServicePortType {

  private static final String EXCEPTION_MESSAGE = "Operation not implemented yet";

  public SKCardServicePortType(SoftKonCore softKonCore) {
    super(softKonCore);
  }

  @Override
  public void verifyPin(
      ContextType context,
      String cardHandle,
      String pinTyp,
      Holder<Status> status,
      Holder<PinResultEnum> pinResult,
      Holder<BigInteger> leftTries) {
    status.value = new Status();
    status.value.setResult("OK");
    leftTries.value = BigInteger.valueOf(3);
    pinResult.value = PinResultEnum.OK;
  }

  @Override
  public void changePin(
      ContextType context,
      String cardHandle,
      String pinTyp,
      Holder<Status> status,
      Holder<PinResultEnum> pinResult,
      Holder<BigInteger> leftTries) {
    throw new NotImplementedException(EXCEPTION_MESSAGE);
  }

  @Override
  public void unblockPin(
      ContextType context,
      String cardHandle,
      String pinTyp,
      Boolean setNewPin,
      Holder<Status> status,
      Holder<PinResultEnum> pinResult,
      Holder<BigInteger> leftTries) {
    throw new NotImplementedException(EXCEPTION_MESSAGE);
  }

  @Override
  public void getPinStatus(
      ContextType context,
      String cardHandle,
      String pinTyp,
      Holder<Status> status,
      Holder<PinStatusEnum> pinStatus,
      Holder<BigInteger> leftTries) {
    throw new NotImplementedException(EXCEPTION_MESSAGE);
  }

  @Override
  public void enablePin(
      ContextType context,
      String cardHandle,
      String pinTyp,
      Holder<Status> status,
      Holder<PinResultEnum> pinResult,
      Holder<BigInteger> leftTries) {
    throw new NotImplementedException(EXCEPTION_MESSAGE);
  }

  @Override
  public void disablePin(
      ContextType context,
      String cardHandle,
      String pinTyp,
      Holder<Status> status,
      Holder<PinResultEnum> pinResult,
      Holder<BigInteger> leftTries) {
    throw new NotImplementedException(EXCEPTION_MESSAGE);
  }
}
