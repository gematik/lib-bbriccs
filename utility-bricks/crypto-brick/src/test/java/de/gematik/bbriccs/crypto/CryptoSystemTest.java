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

package de.gematik.bbriccs.crypto;

import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_RSASSA_PSS;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.sha256WithRSAEncryption;
import static org.bouncycastle.asn1.x9.X9ObjectIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.gematik.bbriccs.crypto.exceptions.InvalidCryptographySpecificationException;
import de.gematik.bbriccs.crypto.exceptions.UnsupportedCryptographySystemException;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.val;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CryptoSystemTest {

  static Stream<Arguments> shouldGetEccFromString() {
    return Stream.of("E256", "ECC_256", "e256", "ecc_256").map(Arguments::of);
  }

  static Stream<Arguments> shouldGetFromOid() {
    return Stream.of(
        Arguments.of(sha256WithRSAEncryption, CryptoSystem.RSA_2048),
        Arguments.of(id_RSASSA_PSS, CryptoSystem.RSA_PSS_2048),
        Arguments.of(ecdsa_with_SHA256, CryptoSystem.ECC_256));
  }

  static Stream<Arguments> shouldGetRsaFromString() {
    return Stream.of("R2048", "RSA_2048", "r2048", "rsa_2048").map(Arguments::of);
  }

  static Stream<Arguments> shouldGetRsaPssFromString() {
    return Stream.of("RSASSA_PSS", "RSASSA-PSS").map(Arguments::of);
  }

  static Stream<Arguments> shouldThrowOnInvalidString() {
    return Stream.of("Ecc256", "ECC_512", "r256", "rsa_256").map(Arguments::of);
  }

  static Stream<Arguments> shouldThrowOnUnsupportedOid() {
    return Stream.of(Arguments.of(cTwoCurve), Arguments.of(ecdsa_with_SHA384));
  }

  @ParameterizedTest
  @MethodSource
  void shouldGetRsaFromString(String input) {
    assertEquals(CryptoSystem.RSA_2048, CryptoSystem.fromString(input));
  }

  @ParameterizedTest
  @MethodSource
  void shouldGetEccFromString(String input) {
    assertEquals(CryptoSystem.ECC_256, CryptoSystem.fromString(input));
  }

  @ParameterizedTest
  @MethodSource
  void shouldGetFromOid(ASN1ObjectIdentifier oid, CryptoSystem expected) {
    val actual = assertDoesNotThrow(() -> CryptoSystem.fromOid(oid));
    assertEquals(expected, actual);
    assertEquals(expected.toString(), actual.toString()); // for coverage only!
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowOnUnsupportedOid(ASN1ObjectIdentifier oid) {
    assertThrows(UnsupportedCryptographySystemException.class, () -> CryptoSystem.fromOid(oid));
  }

  @ParameterizedTest
  @MethodSource
  void shouldGetRsaPssFromString(String input) {
    assertEquals(CryptoSystem.RSA_PSS_2048, CryptoSystem.fromString(input));
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowOnInvalidString(String input) {
    assertThrows(
        UnsupportedCryptographySystemException.class, () -> CryptoSystem.fromString(input));
  }

  @Test
  void fromSpecificationUrn() {
    Arrays.stream(CryptographySpecification.values())
        .forEach(it -> assertDoesNotThrow(() -> CryptoSystem.fromSpecificationUrn(it.getUrn())));
  }

  @Test
  void shouldThrowInvalidCryptographySpecificationException() {
    assertThrows(
        InvalidCryptographySpecificationException.class,
        () -> CryptoSystem.fromSpecificationUrn("urn:unknown"));
  }
}
