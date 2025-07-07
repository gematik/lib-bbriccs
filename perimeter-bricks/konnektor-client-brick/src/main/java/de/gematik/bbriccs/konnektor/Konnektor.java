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

import de.gematik.bbriccs.cardterminal.CardTerminalOperator;
import de.gematik.bbriccs.cardterminal.PinType;
import de.gematik.bbriccs.cardterminal.exceptions.PinVerificationException;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.exceptions.MissingKonnektorServiceException;
import de.gematik.bbriccs.konnektor.requests.ExternalAuthenticateRequest;
import de.gematik.bbriccs.konnektor.requests.GetCardHandleRequest;
import de.gematik.bbriccs.konnektor.requests.VerifyPinRequest;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import java.util.Optional;
import java.util.ServiceLoader;
import lombok.val;

public interface Konnektor {
  org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Konnektor.class);

  String getName();

  <R> KonnektorResponse<R> execute(KonnektorRequest<R> cmd);

  <R> Optional<KonnektorResponse<R>> executeSafely(KonnektorRequest<R> cmd);

  default KonnektorResponse<byte[]> externalAuthenticate(Smartcard smartcard, byte[] challenge) {
    val cryptoSystem = smartcard.getAutCertificate().getCryptoSystem();
    return externalAuthenticate(smartcard, cryptoSystem, challenge);
  }

  default KonnektorResponse<byte[]> externalAuthenticate(
      Smartcard smartcard, CryptoSystem cryptoSystem, byte[] challenge) {
    val cardHandle = this.execute(GetCardHandleRequest.forSmartcard(smartcard)).getPayload();
    val pinResponseType =
        this.execute(new VerifyPinRequest(cardHandle, PinType.PIN_SMC)).getPayload();
    if (pinResponseType.getPinResult() != PinResultEnum.OK) {
      throw new PinVerificationException(smartcard);
    }

    val externalAuthenticateCmd =
        new ExternalAuthenticateRequest(cardHandle, cryptoSystem, challenge);
    return this.execute(externalAuthenticateCmd);
  }

  CardTerminalOperator getCardTerminalOperator();

  static Konnektor create(KonnektorConfiguration cfg) {
    val serviceCfg = cfg.getService();
    val serviceFactory = loadKonnektorService(serviceCfg.getType());
    val kbi = serviceFactory.mapConfiguration(cfg);
    return new KonnektorImpl(kbi.getCtx(), kbi.getServiceProvider(), kbi.getCardTerminals());
  }

  private static KonnektorFactory loadKonnektorService(String named) {
    val loader = ServiceLoader.load(KonnektorFactory.class);
    log.info("Found Konnektor-Services:");
    loader.forEach(ksf -> log.info(ksf.getType()));
    return loader.stream()
        .map(ServiceLoader.Provider::get)
        .filter(ksf -> ksf.getType().equalsIgnoreCase(named))
        .findFirst()
        .orElseThrow(() -> new MissingKonnektorServiceException(named));
  }
}
