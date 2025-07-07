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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.konnektor.vsdm.VsdmService;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SoftKonTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardArchive.fromResources();
  }

  @Test
  void shouldInstantiateSoftKonService() {
    val vsdm = VsdmService.instantiateWithTestKey();
    assertDoesNotThrow(() -> new SofKonServicePort(sca, vsdm));
  }

  /*
  @Test
  void shouldReceiveResponseOnSafeExecute() {
    val mockCmd = mock(GetCardHandleCommand.class);
    val cit = new CardInfoType();
    cit.setIccsn("80276001011699910102");
    cit.setCardHandle("my_test_handle");
    cit.setCtId("Ct01");
    cit.setCardType(CardTypeType.HBA);
    val cardHandle = CardInfo.fromCardInfoType(cit);
    when(mockCmd.execute(any(), any())).thenReturn(cardHandle);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              val response = konnektor.safeExecute(mockCmd);
              assertTrue(response.isPresent());
              assertEquals(cardHandle, response.orElseThrow().getPayload());
            });
  }

  @Test
  void shouldThrowSOAPException() {
    val mockCmd = mock(GetCardHandleCommand.class);
    when(mockCmd.execute(any(), any())).thenThrow(SOAPRequestException.class);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              assertThrows(SOAPRequestException.class, () -> konnektor.execute(mockCmd));
            });
  }

  @Test
  void shouldNotThrowOnSafeExecute() {
    val mockCmd = mock(GetCardHandleCommand.class);
    when(mockCmd.execute(any(), any())).thenThrow(SOAPRequestException.class);

    cfg.getKonnektors()
        .forEach(
            konnektorConfiguration -> {
              val konnektor = KonnektorFactory.createKonnektor(konnektorConfiguration);
              val response = konnektor.safeExecute(mockCmd);
              assertTrue(response.isEmpty());
            });
  }
   */
}
