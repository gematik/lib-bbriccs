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

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CurrencyTest {

  @Test
  void shouldGenerateMoneyWithCurrency() {
    val currency = Currency.EUR;
    val value = 12.0;
    val money = currency.asMoney(value);

    assertEquals("EUR", money.getCurrency());
    assertEquals("12.00", money.getValue().toPlainString());
  }

  @ParameterizedTest(name = "[{index}] round {0} to {1}")
  @CsvSource(
      value = {"12.155/12.16", "10.554/10.55"},
      delimiter = '/')
  void shouldRoundDecimals(double value, String expectation) {
    val currency = Currency.EUR;
    val money = currency.asMoney(value);

    assertEquals("EUR", money.getCurrency());
    assertEquals(expectation, money.getValue().toPlainString());
  }
}
