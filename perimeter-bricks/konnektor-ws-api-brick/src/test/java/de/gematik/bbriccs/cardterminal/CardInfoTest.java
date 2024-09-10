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

package de.gematik.bbriccs.cardterminal;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CardInfoTest {

  @SneakyThrows
  @Test
  void shouldGetFromCardInfoType() {
    val cit = new CardInfoType();
    cit.setCardType(CardTypeType.HBA);
    cit.setCardHandle("handle");
    cit.setIccsn("iccsn");
    cit.setSlotId(BigInteger.ZERO);
    cit.setCtId("ctId");
    cit.setInsertTime(
        DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));

    val ci = Assertions.assertDoesNotThrow(() -> CardInfo.fromCardInfoType(cit));
    Assertions.assertEquals(CardTypeType.HBA, ci.getType());
    assertEquals("handle", ci.getHandle());
    assertEquals("iccsn", ci.getIccsn());
    assertEquals(0, ci.getSlot().intValue());
    assertEquals("ctId", ci.getCtId());
    assertNotNull(ci.getInsertTime());
    assertDoesNotThrow(ci::toString); // just to cover the toString method
  }
}
