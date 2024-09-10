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

package de.gematik.bbriccs.konnektor;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.konnektor.vsdm.VsdmService;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import de.gematik.ws.conn.cardservice.wsdl.v8.CardServicePortType;
import de.gematik.ws.conn.cardterminalservice.wsdl.v1.CardTerminalServicePortType;
import de.gematik.ws.conn.certificateservice.wsdl.v6.CertificateServicePortType;
import de.gematik.ws.conn.encryptionservice.wsdl.v6.EncryptionServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.SignatureServicePortType;
import de.gematik.ws.conn.vsds.vsdservice.v5.VSDServicePortType;
import org.apache.commons.lang3.NotImplementedException;

public class SofKonServicePort extends ServicePort {

  private final SoftKonCore softKonCore;
  private final VsdmService vsdmService;

  public SofKonServicePort(SmartcardArchive smartcardArchive, VsdmService service) {
    super(KonnektorServiceDefinition.forSoftKon());
    this.softKonCore = new SoftKonCore(smartcardArchive);
    this.vsdmService = service;
  }

  public SofKonServicePort(SmartcardArchive smartcardArchive) {
    this(smartcardArchive, VsdmService.instantiateWithTestKey());
  }

  @Override
  public AuthSignatureServicePortType getAuthSignatureService() {
    return new SKAuthSignatureServicePortType(softKonCore);
  }

  @Override
  public CertificateServicePortType getCertificateService() {
    return new SKCertificateServicePortType(softKonCore);
  }

  @Override
  public EventServicePortType getEventService() {
    return new SKEventServicePortType(softKonCore);
  }

  @Override
  public SignatureServicePortType getSignatureService() {
    return new SKSignatureServicePortType(this.softKonCore);
  }

  @Override
  public CardServicePortType getCardService() {
    return new SKCardServicePortType(this.softKonCore);
  }

  @Override
  public CardTerminalServicePortType getCardTerminalService() {
    throw new NotImplementedException("CardTerminalService not implemented yet for SoftKon");
  }

  @Override
  public VSDServicePortType getVSDServicePortType() {
    return new SKVSDServicePortType(this.softKonCore, this.vsdmService);
  }

  @Override
  public EncryptionServicePortType getEncryptionServicePortType() {
    return new SKEncryptionPortType(this.softKonCore);
  }

  @Override
  public String toString() {
    return format("{0}", this.getSds().getProductName());
  }
}
