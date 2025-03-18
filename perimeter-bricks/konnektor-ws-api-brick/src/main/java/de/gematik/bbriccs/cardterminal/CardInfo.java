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

package de.gematik.bbriccs.cardterminal;

import static java.text.MessageFormat.format;

import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Optional;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.val;

@Getter
@Builder
public class CardInfo {

  private final String handle;
  private final String iccsn;

  private final BigInteger slot;
  private final String ctId;
  private final CardTypeType type;
  private final GregorianCalendar insertTime;

  public static CardInfo fromCardInfoType(CardInfoType cit) {
    return new CardInfo.CardInfoBuilder()
        .handle(cit.getCardHandle())
        .iccsn(cit.getIccsn())
        .slot(cit.getSlotId())
        .ctId(cit.getCtId())
        .type(cit.getCardType())
        .insertTime(
            Optional.ofNullable(cit.getInsertTime())
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .orElse(null))
        .build();
  }

  @Override
  public String toString() {
    return format(
        "CardHandle \"{0}\" for {1} with ICCSN {2} in Slot {3}", handle, type, iccsn, slot);
  }

  @Generated
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    val that = (CardInfo) o;

    return iccsn.equals(that.iccsn);
  }

  @Generated
  @Override
  public int hashCode() {
    return iccsn.hashCode();
  }
}
