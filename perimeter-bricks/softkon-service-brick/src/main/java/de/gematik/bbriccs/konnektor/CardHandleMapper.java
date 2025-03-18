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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservice.v8.Cards;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.val;
import one.util.streamex.EntryStream;

@Getter
public class CardHandleMapper {

  private final Cards cards;
  private final Map<String, CardInfoType> cardsMap;

  public CardHandleMapper(SmartcardArchive sca) {
    this(sca.getConfigs());
  }

  public CardHandleMapper(List<SmartcardConfigDto> smartcardConfigDtos) {
    this.cards = new Cards();
    val cardInfos =
        EntryStream.of(smartcardConfigDtos)
            .mapKeyValue(CardHandleMapper::createCardInfoType)
            .toList();
    this.cardsMap =
        cardInfos.stream().collect(Collectors.toMap(CardInfoType::getCardHandle, cit -> cit));
    this.cards.getCard().addAll(cardInfos);
  }

  public String getIccsnByCardHandle(String cardHandle) {
    return cardsMap.get(cardHandle).getIccsn();
  }

  private static CardInfoType createCardInfoType(int slotId, SmartcardConfigDto scfg) {
    val cit = new CardInfoType();
    cit.setCardType(mapSmartcardType(scfg.getType()));
    cit.setIccsn(scfg.getIccsn());
    cit.setCardHandle(createCardHandleString(scfg));
    cit.setSlotId(BigInteger.valueOf(slotId));
    cit.setCtId("SoftKonKT");

    if (scfg.getType().equals(SmartcardType.EGK)) {
      cit.setKvnr(scfg.getIdentifier());
    }
    return cit;
  }

  private static CardTypeType mapSmartcardType(SmartcardType type) {
    return switch (type) {
      case EGK -> CardTypeType.EGK;
      case SMC_B -> CardTypeType.SMC_B;
      case HBA -> CardTypeType.HBA;
      case SMC_KT -> CardTypeType.SMC_KT;
    };
  }

  private static String createCardHandleString(SmartcardConfigDto scfg) {
    return format("{0}_{1}", scfg.getType(), scfg.getIccsn());
  }
}
