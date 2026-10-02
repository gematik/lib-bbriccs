/*
 * Copyright (Change Date see Readme) gematik GmbH
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

import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.ws.conn.cardservice.v8_2.SecureSendAPDUResponse;
import de.gematik.ws.conn.cardservice.v8_2.SignedScenarioResponseType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import jakarta.xml.ws.Holder;
import lombok.val;

public class SecureSendAPDURequest extends AbstractKonnektorRequest<SecureSendAPDUResponse> {

  private final String signedScenario;

  public SecureSendAPDURequest(String signedScenario) {
    this.signedScenario = signedScenario;
  }

  @Override
  public SecureSendAPDUResponse execute(ContextType ctx, ServicePort serviceProvider) {
    val holderStatus = new Holder<Status>();
    val holderSignedScenarioResponse = new Holder<SignedScenarioResponseType>();
    this.executeAction(
        () ->
            serviceProvider
                .getCardService()
                .secureSendAPDU(signedScenario, holderStatus, holderSignedScenarioResponse));
    val response = new SecureSendAPDUResponse();
    response.setStatus(holderStatus.value);
    response.setSignedScenarioResponse(holderSignedScenarioResponse.value);
    return response;
  }
}
