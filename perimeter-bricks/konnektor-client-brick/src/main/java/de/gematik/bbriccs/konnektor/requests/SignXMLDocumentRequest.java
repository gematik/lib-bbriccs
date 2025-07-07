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
import de.gematik.bbriccs.konnektor.exceptions.SOAPRequestException;
import de.gematik.bbriccs.konnektor.requests.options.SignDocumentOptions;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.ObjectFactory;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

@Slf4j
public class SignXMLDocumentRequest extends AbstractKonnektorRequest<byte[]> {

  private final CardInfo cardInfo;
  private final SignRequest signRequest;
  private final SignDocumentOptions options;

  public SignXMLDocumentRequest(CardInfo cardInfo, String content, CryptoSystem cryptoSystem) {
    this(cardInfo, content, cryptoSystem, false);
  }

  public SignXMLDocumentRequest(
      CardInfo cardInfo, String content, CryptoSystem cryptoSystem, boolean includeRevocationInfo) {
    this(
        cardInfo,
        content.getBytes(StandardCharsets.UTF_8),
        SignDocumentOptions.withAlgorithm(cryptoSystem),
        includeRevocationInfo);
  }

  public SignXMLDocumentRequest(
      CardInfo cardInfo,
      byte[] content,
      SignDocumentOptions options,
      boolean includeRevocationInfo) {
    val factory = new ObjectFactory();
    this.options = options;
    this.cardInfo = cardInfo;
    this.signRequest = factory.createSignRequest();
    this.signRequest.setIncludeRevocationInfo(includeRevocationInfo);

    val doctype = factory.createDocumentType();
    doctype.setID("CMS-Doc1"); // what about this one?
    doctype.setShortText("a CMSDocument2Sign");

    val data = new Base64Data();
    data.setMimeType(options.getMimeType());
    data.setValue(content);
    doctype.setBase64Data(data);

    signRequest.setDocument(doctype);

    val optVal = factory.createSignRequestOptionalInputs();
    optVal.setIncludeEContent(options.isIncludeEContent());
    optVal.setSignatureType(options.getSignatureType().getUrn());
    signRequest.setOptionalInputs(optVal);
  }

  @Override
  public byte[] execute(ContextType ctx, ServicePort serviceProvider) {
    val servicePort = serviceProvider.getSignatureService();
    val requestId = UUID.randomUUID().toString();
    signRequest.setRequestID(requestId);

    val jobNumber = this.executeSupplier(() -> servicePort.getJobNumber(ctx));

    log.trace(
        format(
            "Sign XML Document with {0} for JobNumber {1} and RequestID {2}",
            options.getCryptoType().getValue(), jobNumber, requestId));

    val response =
        this.executeSupplier(
            () ->
                servicePort.signDocument(
                    cardInfo.getHandle(),
                    options.getCryptoType().getValue(),
                    ctx,
                    options.getTvMode(),
                    jobNumber,
                    List.of(signRequest)));

    val signedDoc =
        response.stream()
            .filter(rsp -> rsp.getRequestID().equals(requestId))
            .findAny()
            .orElseThrow(
                () ->
                    new SOAPRequestException(
                        this.getClass(),
                        format("Response does not contain any entry for Request {0}", requestId)));

    return signedDoc.getSignatureObject().getBase64Signature().getValue();
  }
}
