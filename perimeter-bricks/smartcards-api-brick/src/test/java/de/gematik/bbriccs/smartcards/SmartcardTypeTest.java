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

package de.gematik.bbriccs.smartcards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
      Class<Smartcard> clazz, SmartcardType expectedType) {
    val type = SmartcardType.fromImplementationType(clazz);
    assertEquals(expectedType, type);
  }

  static Stream<Arguments> shouldMapSmartcardTypeFromImplementationClass() {
    return Stream.of(
        Arguments.of(Egk.class, SmartcardType.EGK),
        Arguments.of(Hba.class, SmartcardType.HBA),
        Arguments.of(SmcB.class, SmartcardType.SMC_B));
  }

  @Test
  void shouldThrowOnUnknownSmartcardImplementation() {
    assertThrows(
        InvalidSmartcardTypeException.class,
        () -> SmartcardType.fromImplementationType(UnknownSmartcard.class));
  }

  @ParameterizedTest
  @MethodSource
  void shouldProvideName(SmartcardType type, String expectedName) {
    assertEquals(expectedName, type.getName());
    assertTrue(type.toString().contains(expectedName));
  }

  static Stream<Arguments> shouldProvideName() {
    return Stream.of(
        Arguments.of(SmartcardType.EGK, "eGK"),
        Arguments.of(SmartcardType.HBA, "HBA"),
        Arguments.of(SmartcardType.SMC_B, "SMC-B"),
        Arguments.of(SmartcardType.SMC_KT, "SMC-KT"));
  }

  private static class UnknownSmartcard implements Smartcard {

    protected UnknownSmartcard() {}

    @Override
    public SmartcardCertificate getAutCertificate() {
      return null;
    }

    @Override
    public Optional<SmartcardCertificate> getAutCertificate(CryptoSystem cryptoSystem) {
      return Optional.empty();
    }

    @Override
    public PrivateKey getAuthPrivateKey() {
      return null;
    }

    @Override
    public PublicKey getAuthPublicKey() {
      return null;
    }

    @Override
    public String getPrivateKeyBase64() {
      return "";
    }

    @Override
    public List<Oid> getAutOids() {
      return List.of();
    }

    @Override
    public String getIccsn() {
      return "123";
    }

    @Override
    public SmartcardType getType() {
      return SmartcardType.SMC_KT;
    }

    @Override
    public SmartcardOwnerData getOwnerData() {
      return null;
    }

    @Override
    public Map<String, Object> getExtension() {
      return Map.of();
    }

    @Override
    public <E extends SmartcardExtension> E getExtensionAs(Class<E> extensionType) {
      return null;
    }
  }
}
