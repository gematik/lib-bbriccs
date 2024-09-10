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

package de.gematik.bbriccs.smartcards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SmartcardTypeTest {

  @Test
  void shouldGetSmartcardTypeFromString() {
    val inputs =
        Map.of(
            "egk",
            SmartcardType.EGK,
            "hba",
            SmartcardType.HBA,
            "smcb",
            SmartcardType.SMC_B,
            "smc-b",
            SmartcardType.SMC_B,
            "smckt",
            SmartcardType.SMC_KT,
            "EGK",
            SmartcardType.EGK,
            "h-b-a",
            SmartcardType.HBA,
            "HbA",
            SmartcardType.HBA);

    inputs.forEach((k, v) -> assertEquals(v, SmartcardType.fromString(k)));
  }

  @Test
  void shouldThrowOnSmartcardTypeNull() {
    assertThrows(NullPointerException.class, () -> SmartcardType.fromString(null));
  }

  @Test
  void shouldThrowOnInvalidSmartcardType() {
    val inputs = List.of("smc-a", "SMC-D", "egk2");
    inputs.forEach(
        input ->
            assertThrows(
                InvalidSmartcardTypeException.class, () -> SmartcardType.fromString("SMC-D")));
  }

  @ParameterizedTest
  @MethodSource
  void shouldMapSmartcardTypeFromImplementationClass(
      Class<SmartcardP12> clazz, SmartcardType expectedType) {
    val type = SmartcardType.fromImplementationType(clazz);
    assertEquals(expectedType, type);
  }

  static Stream<Arguments> shouldMapSmartcardTypeFromImplementationClass() {
    return Stream.of(
        Arguments.of(EgkP12.class, SmartcardType.EGK),
        Arguments.of(HbaP12.class, SmartcardType.HBA),
        Arguments.of(SmcBP12.class, SmartcardType.SMC_B));
  }

  @Test
  void shouldThrowOnUnknownSmartcardImplementation() {
    assertThrows(
        InvalidSmartcardTypeException.class,
        () -> SmartcardType.fromImplementationType(UnknownSmartcard.class));
  }

  private static class UnknownSmartcard extends SmartcardP12 {

    protected UnknownSmartcard() {
      super(SmartcardType.SMC_KT, new SmartcardConfigDto(), List.of());
    }

    @Override
    public List<Oid> getAutOids() {
      return List.of();
    }
  }
}
