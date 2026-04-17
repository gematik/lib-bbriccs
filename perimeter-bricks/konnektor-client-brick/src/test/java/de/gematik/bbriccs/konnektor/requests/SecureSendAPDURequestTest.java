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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.ws.conn.cardservice.v8_2.SignedScenarioResponseType;
import de.gematik.ws.conn.cardservice.wsdl.v8_2.CardServicePortType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import jakarta.xml.ws.Holder;
import java.math.BigInteger;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecureSendAPDURequestTest {

  private static final String SIGNED_SCENARIO =
      "eyJ4NWMiOlsiTUlJQzVEQ0NBb3VnQXdJQkFnSUhBWkpJWUxPZ0REQUtCZ2dxaGtqT1BRUURBakNCaERFTE1Ba0dBMVVFQmhNQ1JFVXhIekFkQmdOVkJBb01GbWRsYldGMGFXc2dSMjFpU0NCT1QxUXRWa0ZNU1VReE1qQXdCZ05WQkFzTUtVdHZiWEJ2Ym1WdWRHVnVMVU5CSUdSbGNpQlVaV3hsYldGMGFXdHBibVp5WVhOMGNuVnJkSFZ5TVNBd0hnWURWUVFEREJkSFJVMHVTMDlOVUMxRFFUWXhJRlJGVTFRdFQwNU1XVEFlRncweU5UQXhNall5TXpBd01EQmFGdzB6TURBeE1qWXlNalU1TlRsYU1Gc3hDekFKQmdOVkJBWVRBa1JGTVNZd0pBWURWUVFLREIxblpXMWhkR2xySUZSRlUxUXRUMDVNV1NBdElFNVBWQzFXUVV4SlJERWtNQ0lHQTFVRUF3d2JjRzl3Y0M1blpXMWhkR2xyTG5SbGJHVnRZWFJwYXkxMFpYTjBNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUUrU3dEV0RYTEFtVlVhQ0U3VjZkQkpKWWZRQkVUQXkwa0R4MHFEM2pqOTFyR01QNEdHYnFoUFNBQlA0Qll6MG9nWmRuaGlkRDlxbXRhTjMxVWx6TkdsYU9DQVE0d2dnRUtNQXdHQTFVZEV3RUIvd1FDTUFBd0lRWURWUjBnQkJvd0dEQUtCZ2dxZ2hRQVRBU0NIekFLQmdncWdoUUFUQVNCSXpBN0JnZ3JCZ0VGQlFjQkFRUXZNQzB3S3dZSUt3WUJCUVVITUFHR0gyaDBkSEE2THk5bGFHTmhMbWRsYldGMGFXc3VaR1V2WldOakxXOWpjM0F3RGdZRFZSMFBBUUgvQkFRREFnWkFNQjBHQTFVZERnUVdCQlNjSWtyb3hTTmdaaHAvWnFsNmRvSXhCV2hvT0RCS0JnVXJKQWdEQXdSQk1EOHdQVEE3TURrd056QXBEQ2RRY205dlppQnZaaUJRWVhScFpXNTBJRkJ5WlhObGJtTmxJQ2hRYjFCUUtTQkVhV1Z1YzNRd0NnWUlLb0lVQUV3RWdpVXdId1lEVlIwakJCZ3dGb0FVbnpYZ01LbC95dmhtbjVBS1FzMjdnV1dmU2Y0d0NnWUlLb1pJemowRUF3SURSd0F3UkFJZ0VzWi84RUI3REQ1UGEwMU03Rkl6TFZaZUdKUU5aTklaNWxGWXpCQVpuZHNDSUgzTGRrNGwxdFUzSEJNZmhacnJtczE5ZFVNcml4UmFpN29zczV5dDNtalQiXSwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYiLCJ4NXQjUzI1NiI6ImZaeFlMQ2tLRkV2a2hIaEJhbFl5eVUzTlFLU2dwTS1JcVBDMVFWMjJhQlUifQ"
          + ".eyJzdGFuZGFyZFNjZW5hcmlvTWVzc2FnZSI6eyJ0eXBlIjoiU3RhbmRhcmRTY2VuYXJpbyIsInZlcnNpb24iOiIxLjAuMCIsImNsaWVudFNlc3Npb25JZCI6ImZjNDQyM2FlLWVkMDctNDFjMy05YzBlLWExMTViZjViNmExZCIsInNlcXVlbmNlQ291bnRlciI6MCwidGltZVNwYW4iOjEwMDAwLCJzdGVwcyI6W3siYXBkdUNvbW1hbmQiOiIwMCBhNCAwNDBjICAgIDA3IEQyNzYwMDAxNDQ4MDAwIiwiZXhwZWN0ZWRTdGF0dXNXb3JkcyI6WyI5MDAwIl19LHsiYXBkdUNvbW1hbmQiOiIwMCBiMCA5MTAwICAgIDAwIiwiZXhwZWN0ZWRTdGF0dXNXb3JkcyI6WyI5MDAwIiwiNjI4MSJdfV19fQ"
          + ".cbnxYTWUR4-qplK_pt9zYbzg5Dx9UPhhazPQk5d9Ghqu-FshkAqdPyERApzTTOa5ksttH_-TS-fYWARjPSoF0A";

  private ServicePort servicePort;
  private CardServicePortType cardService;

  @BeforeEach
  void setUp() {
    servicePort = mock(ServicePort.class);
    cardService = mock(CardServicePortType.class);
    when(servicePort.getCardService()).thenReturn(cardService);
  }

  @Test
  void shouldExecuteSecureSendAPDUAndReturnResponse() throws Exception {
    // Arrange
    val signedScenarioResponse = buildSignedScenarioResponse("9000");

    doAnswer(
            invocation -> {
              Holder<Status> statusHolder = invocation.getArgument(1);
              Holder<SignedScenarioResponseType> responseHolder = invocation.getArgument(2);

              val status = new Status();
              status.setResult("OK");
              statusHolder.value = status;

              responseHolder.value = signedScenarioResponse;
              return null;
            })
        .when(cardService)
        .secureSendAPDU(eq(SIGNED_SCENARIO), any(), any());

    val request = new SecureSendAPDURequest(SIGNED_SCENARIO);

    val response = assertDoesNotThrow(() -> request.execute(null, servicePort));

    assertNotNull(response);
    assertEquals("OK", response.getStatus().getResult());

    assertNotNull(response.getSignedScenarioResponse());
    val apduList = response.getSignedScenarioResponse().getResponseApduList();
    assertFalse(apduList.getResponseApdu().isEmpty());
    assertEquals("9000", apduList.getResponseApdu().get(0));

    assertNotNull(response.getSignedScenarioResponse().getTimeSpan());
    assertEquals(BigInteger.valueOf(10000), response.getSignedScenarioResponse().getTimeSpan());

    verify(cardService).secureSendAPDU(eq(SIGNED_SCENARIO), any(), any());
  }

  @Test
  void shouldPassSignedScenarioToCardService() throws Exception {
    // Arrange
    doAnswer(
            invocation -> {
              Holder<Status> statusHolder = invocation.getArgument(1);
              Holder<SignedScenarioResponseType> responseHolder = invocation.getArgument(2);

              statusHolder.value = new Status();
              responseHolder.value = buildSignedScenarioResponse("9000");
              return null;
            })
        .when(cardService)
        .secureSendAPDU(any(), any(), any());

    val request = new SecureSendAPDURequest(SIGNED_SCENARIO);

    request.execute(null, servicePort);
    verify(cardService).secureSendAPDU(eq(SIGNED_SCENARIO), any(), any());
  }

  private SignedScenarioResponseType buildSignedScenarioResponse(String... apdus) {
    val response = new SignedScenarioResponseType();

    val apduList = new SignedScenarioResponseType.ResponseApduList();
    for (String apdu : apdus) {
      apduList.getResponseApdu().add(apdu);
    }
    response.setResponseApduList(apduList);
    response.setTimeSpan(BigInteger.valueOf(10000));

    return response;
  }
}
