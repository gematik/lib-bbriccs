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

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.encryption.cms.CmsAuthEnvelopedData;
import de.gematik.bbriccs.smartcards.InstituteSmartcard;
import de.gematik.bbriccs.smartcards.InstituteSmartcardP12;
import de.gematik.ws.conn.connectorcommon.v5.DocumentType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.encryptionservice.v6.EncryptDocument.OptionalInputs;
import de.gematik.ws.conn.encryptionservice.v6.EncryptDocument.RecipientKeys;
import de.gematik.ws.conn.encryptionservice.v6.KeyOnCardType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import jakarta.xml.ws.Holder;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;

public class SKEncryptionPortType extends SoftKonServicePortType
    implements EncryptionServicePortType {
  public SKEncryptionPortType(SoftKonCore softKonCore) {
    super(softKonCore);
  }

  @SneakyThrows
  @Override
  public void encryptDocument(
      ContextType context,
      RecipientKeys recipientKeys,
      Holder<DocumentType> document,
      OptionalInputs optionalInputs,
      Holder<Status> status,
      Holder<Object> optionalOutputs) {

    val cardHandle = recipientKeys.getCertificateOnCard().getCardHandle();
    val smartcard = this.softKonCore.getSmartcardByCardHandle(InstituteSmartcard.class, cardHandle);

    val plain = document.value.getBase64Data().getValue();

    // TODO: the SoftKon currently supports only RSA for encryption/decryption...
    val algorithm = CryptoSystem.RSA_2048;

    val cmsAuthEnvelopedData = new CmsAuthEnvelopedData();
    val decrypted =
        cmsAuthEnvelopedData.encrypt(
            List.of(smartcard.getEncCertificate(algorithm).getX509Certificate()), plain);
    val base64Data = new Base64Data();
    base64Data.setValue(decrypted);
    document.value.setBase64Data(base64Data);
  }

  @SneakyThrows
  @Override
  public void decryptDocument(
      ContextType context,
      KeyOnCardType privateKeyOnCard,
      Holder<DocumentType> document,
      Object optionalInputs,
      Holder<Status> status,
      Holder<Object> optionalOutputs) {

    val cardHandle = privateKeyOnCard.getCardHandle();
    val smartcard =
        this.softKonCore.getSmartcardByCardHandle(InstituteSmartcardP12.class, cardHandle);

    val encrypted = document.value.getBase64Data().getValue();

    // TODO: the SoftKon currently supports only RSA for encryption/decryption...
    val algorithm = CryptoSystem.RSA_2048;

    val cmsAuthEnvelopedData = new CmsAuthEnvelopedData();
    val decrypted =
        cmsAuthEnvelopedData.decrypt(
            smartcard.getEncCertificate(algorithm).getPrivateKey(), encrypted);
    val base64Data = new Base64Data();
    base64Data.setValue(decrypted);
    document.value.setBase64Data(base64Data);
  }
}
