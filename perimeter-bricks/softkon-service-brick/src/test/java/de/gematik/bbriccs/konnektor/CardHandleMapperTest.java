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
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CardHandleMapperTest {

  static Stream<Arguments> shouldMapSmartcardType() {
    return Stream.of(
        Arguments.of(SmartcardType.EGK, CardTypeType.EGK),
        Arguments.of(SmartcardType.SMC_B, CardTypeType.SMC_B),
        Arguments.of(SmartcardType.HBA, CardTypeType.HBA),
        Arguments.of(SmartcardType.SMC_KT, CardTypeType.SMC_KT));
  }

  @ParameterizedTest
  @MethodSource
  void shouldMapSmartcardType(SmartcardType input, CardTypeType expected) {
    val sccfg = new SmartcardConfigDto();
    sccfg.setType(input);

    val chm = assertDoesNotThrow(() -> new CardHandleMapper(List.of(sccfg)));
    assertEquals(1, chm.getCards().getCard().size());
    assertEquals(expected, chm.getCards().getCard().get(0).getCardType());
  }

  @Test
  void shouldGetIccsnByCardHandle() {
    val iccsn = "123123123123";
    val sccfg = new SmartcardConfigDto();
    sccfg.setType(SmartcardType.EGK);
    sccfg.setIccsn(iccsn);

    val chm = assertDoesNotThrow(() -> new CardHandleMapper(List.of(sccfg)));
    assertEquals(1, chm.getCards().getCard().size());
    assertEquals(iccsn, chm.getIccsnByCardHandle(format("{0}_{1}", sccfg.getType(), iccsn)));
  }
}
