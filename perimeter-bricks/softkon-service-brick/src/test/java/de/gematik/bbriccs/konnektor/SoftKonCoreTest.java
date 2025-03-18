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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.*;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.signatureservice.wsdl.v7.FaultMessage;
import javax.xml.datatype.DatatypeFactory;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SoftKonCoreTest {

  @Test
  void shouldGetCards() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    val cards = assertDoesNotThrow(skc::getAllCards);
    assertNotNull(cards);
  }

  @Test
  void shouldGetSmartcardByHandle() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    val cards = assertDoesNotThrow(skc::getAllCards);
    val egkInfoType =
        cards.getCard().stream()
            .filter(cit -> cit.getCardType().equals(CardTypeType.HBA))
            .findFirst()
            .orElseThrow();
    val smartcard =
        assertDoesNotThrow(
            () -> skc.getSmartcardByCardHandleSafely(HbaP12.class, egkInfoType.getCardHandle()));
    assertTrue(smartcard.isPresent());
  }

  @ParameterizedTest
  @ValueSource(classes = {EgkP12.class, HbaP12.class, SmcBP12.class})
  void shouldThrowOnUnknownHandle(Class<SmartcardP12> smartcardClass) {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    assertThrows(
        CardNotFoundException.class, () -> skc.getSmartcardByCardHandle(smartcardClass, "unknown"));
  }

  @Test
  void shouldThrowOnSignWithUnknownCardHandle() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    assertThrows(
        FaultMessage.class,
        () ->
            skc.signDocumentWith("unkown", CryptoSystem.RSA_2048, false, "HelloWorld".getBytes()));
  }

  @Test
  void shouldThrowOnSignWithEgk() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    val cards = assertDoesNotThrow(skc::getAllCards);
    val egkInfoType =
        cards.getCard().stream()
            .filter(cit -> cit.getCardType().equals(CardTypeType.EGK))
            .findFirst()
            .orElseThrow();
    assertThrows(
        FaultMessage.class,
        () ->
            skc.signDocumentWith(
                egkInfoType.getCardHandle(),
                CryptoSystem.RSA_2048,
                false,
                "HelloWorld".getBytes()));
  }

  @Test
  void shouldSignAndVerify() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    val cards = assertDoesNotThrow(skc::getAllCards);
    val hbaCardInfo =
        cards.getCard().stream()
            .filter(cit -> cit.getCardType().equals(CardTypeType.HBA))
            .findFirst()
            .orElseThrow();

    val signed =
        assertDoesNotThrow(
            () ->
                skc.signDocumentWith(
                    hbaCardInfo.getCardHandle(),
                    CryptoSystem.RSA_2048,
                    false,
                    "HelloWorld".getBytes()));
    assertTrue(skc.verifyDocument(signed));
  }

  @Test
  void shouldIncrementJobNumber() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    val j1 = skc.getJobNumber(null);
    val j2 = skc.getJobNumber(null);
    assertFalse(j1.isEmpty());
    assertFalse(j2.isEmpty());
    assertNotEquals(j1, j2);
  }

  @Test
  void shouldReThrowDatatypeError() {
    val skc = new SoftKonCore(SmartcardArchive.fromResources());
    try (val msdtf = mockStatic(DatatypeFactory.class)) {
      msdtf.when(DatatypeFactory::newInstance).thenThrow(new RuntimeException("Test"));
      assertThrows(RuntimeException.class, () -> skc.createError("test"));
    }
  }
}
