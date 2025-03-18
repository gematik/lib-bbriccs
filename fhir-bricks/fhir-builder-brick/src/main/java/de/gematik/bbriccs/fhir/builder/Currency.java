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

package de.gematik.bbriccs.fhir.builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Money;

@Getter
@AllArgsConstructor
public enum Currency {
  EUR("EUR", "€", "Euro");

  private final String code;
  private final String symbol;
  private final String name;

  public Money asMoney(double value) {
    return asMoney(BigDecimal.valueOf(value));
  }

  public Money asMoney(BigDecimal value) {
    // currency values are always formatted with two decimal places
    val fValue = value.setScale(2, RoundingMode.HALF_UP);
    return new Money().setValue(fValue).setCurrency(this.getCode());
  }
}
