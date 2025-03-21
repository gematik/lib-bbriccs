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

package de.gematik.bbriccs.konnektor.requests;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.cardterminal.CardInfo;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.bbriccs.konnektor.exceptions.SOAPRequestException;
import de.gematik.ws.conn.certificateservice.v6.CryptType;
import de.gematik.ws.conn.certificateservice.v6.ObjectFactory;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import jakarta.xml.ws.Holder;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ReadCardCertificateRequest extends AbstractKonnektorRequest<X509Certificate> {

  private final CardInfo cardInfo;
  private final CertRefEnum certRef;
  private final CryptType cryptType;

  /**
   * Reads the C_AUT certificate from given card
   *
   * @param cardInfo identifies the card on the Konnektor
   */
  public ReadCardCertificateRequest(CardInfo cardInfo, CryptoSystem cryptoSystem) {
    this(cardInfo, CertRefEnum.C_AUT, cryptoSystem);
  }

  public ReadCardCertificateRequest(
      CardInfo cardInfo, CertRefEnum certRef, CryptoSystem cryptoSystem) {
    this(cardInfo, certRef, getCryptoType(cryptoSystem));
  }

  private static CryptType getCryptoType(CryptoSystem cryptoSystem) {
    return switch (cryptoSystem) {
      case RSA_2048, RSA_PSS_2048 -> CryptType.RSA;
      default -> CryptType.ECC;
    };
  }

  private ReadCardCertificateRequest(CardInfo cardInfo, CertRefEnum certRef, CryptType cryptType) {
    this.cardInfo = cardInfo;
    this.certRef = certRef;
    this.cryptType = cryptType;
  }

  @Override
  @SneakyThrows
  public X509Certificate execute(ContextType ctx, ServicePort serviceProvider) {
    log.trace(format("Read {0} Certificate from Card {1}", certRef, cardInfo));
    val servicePort = serviceProvider.getCertificateService();
    val factory = new ObjectFactory();

    val certRefList = factory.createReadCardCertificateCertRefList();
    certRefList.getCertRef().add(certRef);

    val outStatus = new Holder<Status>();
    val outX509DataInfoList = new Holder<X509DataInfoListType>();

    this.executeAction(
        () ->
            servicePort.readCardCertificate(
                cardInfo.getHandle(), ctx, certRefList, cryptType, outStatus, outX509DataInfoList));

    val x509Bytes =
        outX509DataInfoList.value.getX509DataInfo().stream()
            .filter(di -> di.getCertRef().equals(certRef))
            .map(di -> di.getX509Data().getX509Certificate())
            .findFirst()
            .orElseThrow(
                () ->
                    new SOAPRequestException(
                        this.getClass(),
                        format(
                            "Response does not contain any {0} certificate for {1}",
                            certRef, cardInfo)));

    val certFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(x509Bytes));
  }
}
