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

import de.gematik.bbriccs.cardterminal.CardInfo;
import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.ws.conn.connectorcontext.v2.*;
import de.gematik.ws.conn.vsds.vsdservice.v5.*;
import jakarta.xml.ws.Holder;
import lombok.*;

public class ReadVsdRequest extends AbstractKonnektorRequest<ReadVSDResponse> {

  private final CardInfo hpcCardInfo;
  private final CardInfo egkCardInfo;
  private final boolean withPerformOnlineCheck;
  private final boolean withOnlineReceipt;

  public ReadVsdRequest(
      CardInfo egkCardInfo,
      CardInfo hpcCardInfo,
      boolean withPerformOnlineCheck,
      boolean withOnlineReceipt) {
    this.egkCardInfo = egkCardInfo;
    this.hpcCardInfo = hpcCardInfo;
    this.withPerformOnlineCheck = withPerformOnlineCheck;
    this.withOnlineReceipt = withOnlineReceipt;
  }

  @Override
  public ReadVSDResponse execute(ContextType ctx, ServicePort serviceProvider) {
    val servicePort = serviceProvider.getVSDServicePortType();

    val readVsdResponse = new ReadVSDResponse();
    val personalInsuranceData = new Holder<byte[]>();
    val commonInsuranceData = new Holder<byte[]>();
    val protectedInsuranceData = new Holder<byte[]>();
    val vsdStatus = new Holder<VSDStatusType>();
    val evidence = new Holder<byte[]>();

    this.executeAction(
        () ->
            servicePort.readVSD(
                egkCardInfo.getHandle(),
                hpcCardInfo.getHandle(),
                withPerformOnlineCheck,
                withOnlineReceipt,
                ctx,
                personalInsuranceData,
                commonInsuranceData,
                protectedInsuranceData,
                vsdStatus,
                evidence));

    readVsdResponse.setPersoenlicheVersichertendaten(personalInsuranceData.value);
    readVsdResponse.setAllgemeineVersicherungsdaten(commonInsuranceData.value);
    readVsdResponse.setGeschuetzteVersichertendaten(protectedInsuranceData.value);
    readVsdResponse.setVSDStatus(vsdStatus.value);
    readVsdResponse.setPruefungsnachweis(evidence.value);
    return readVsdResponse;
  }
}