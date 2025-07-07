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

package de.gematik.bbriccs.konnektor.requests;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.cardterminal.CardInfo;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.bbriccs.konnektor.requests.options.SignatureType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7_4.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate.OptionalInputs;
import jakarta.xml.ws.Holder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
public class ExternalAuthenticateRequest extends AbstractKonnektorRequest<byte[]> {

  private final CardInfo cardInfo;
  private final CryptoSystem cryptoSystem;
  private final byte[] toBeSignedData;

  public ExternalAuthenticateRequest(
      CardInfo cardInfo, CryptoSystem cryptoSystem, byte[] toBeSignedData) {
    this.cardInfo = cardInfo;
    this.cryptoSystem = cryptoSystem;
    this.toBeSignedData = toBeSignedData;
  }

  @Override
  public byte[] execute(ContextType ctx, ServicePort serviceProvider) {
    log.trace(
        format(
            "External Authenticate with CardHandle {0} and challenge of length {1}",
            cardInfo.getHandle(), toBeSignedData.length));
    val servicePort = serviceProvider.getAuthSignatureService();

    val binaryDocument = new BinaryDocumentType();

    val base64Data = new Base64Data();
    base64Data.setValue(DigestUtils.sha256(toBeSignedData));
    base64Data.setMimeType("application/octet-stream");
    binaryDocument.setBase64Data(base64Data);

    val optionalInputs = new OptionalInputs();

    if (this.cryptoSystem == CryptoSystem.RSA_2048
        || this.cryptoSystem == CryptoSystem.RSA_PSS_2048) {
      optionalInputs.setSignatureType(SignatureType.RFC_3447.getUrn());
      optionalInputs.setSignatureSchemes(CryptoSystem.RSA_PSS_2048.getName());
    } else if (cryptoSystem == CryptoSystem.ECC_256) {
      optionalInputs.setSignatureType(SignatureType.BSI_TR_03111.getUrn());
    }

    val outStatus = new Holder<Status>();
    val signatureObject = new Holder<SignatureObject>();

    this.executeAction(
        () ->
            servicePort.externalAuthenticate(
                cardInfo.getHandle(),
                ctx,
                optionalInputs,
                binaryDocument,
                outStatus,
                signatureObject));

    return signatureObject.value.getBase64Signature().getValue();
  }
}
