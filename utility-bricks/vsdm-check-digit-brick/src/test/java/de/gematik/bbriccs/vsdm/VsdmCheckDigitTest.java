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

package de.gematik.bbriccs.vsdm;

import static de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion.V1;
import static de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion.V2;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.vsdm.types.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VsdmCheckDigitTest {
  private static final byte[] KEY = new byte[32];

  static {
    KEY[31] = (byte) 0x01;
  }

  private static final VsdmKey KEY_V1 = new VsdmKey(KEY, new VsdmKeyVersion('1', V1));
  private static final VsdmKey KEY_V2 = new VsdmKey(KEY, new VsdmKeyVersion('2', V2));

  @Test
  void shouldSignDheckDigitV1() {
    val checkDigit = VsdmCheckDigitFactory.createV1("X123456789", 'S');
    Assertions.assertAll(
        () -> Assertions.assertDoesNotThrow(() -> checkDigit.sign(KEY_V1)),
        () -> Assertions.assertEquals("X123456789", checkDigit.getPatient().getKvnr()),
        () ->
            Assertions.assertEquals(
                checkDigit.getIdentifier(), VsdmVendorIdentifier.from('S', V1)));
  }

  @Test
  void shouldParseCheckDigitV1() {
    val checkDigit = VsdmCheckDigitFactory.createV1("X123456789", 'S');
    val base64 = checkDigit.sign(KEY_V1);
    Assertions.assertDoesNotThrow(() -> VsdmCheckDigit.parse(base64));

    val checkDigit2 = VsdmCheckDigit.parse(base64);
    Assertions.assertAll(
        () ->
            Assertions.assertEquals(
                checkDigit.getPatient().getKvnr(),
                checkDigit2.getPatient().getKvnr(),
                "Kvnr is not the same"),
        () ->
            Assertions.assertEquals(
                checkDigit.getIdentifier(),
                checkDigit2.getIdentifier(),
                "Identifier is not the same"),
        () ->
            Assertions.assertEquals(
                0,
                checkDigit2
                    .getIatTimestamp()
                    .compareIatTimestampWith(checkDigit.getIatTimestamp().getTimestamp()),
                "IatTime is not the same"));
  }

  @Test
  void shouldEncryptCheckDigitV2() {
    val now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    val patient =
        new VsdmPatient(VsdmKvnr.from("X123456789"), true, LocalDate.now(), "ExampleStreet");
    val checkDigit = VsdmCheckDigitFactory.createV2(patient, 'A').setIatTimestamp(now);
    Assertions.assertAll(
        () -> Assertions.assertDoesNotThrow(() -> checkDigit.encrypt(KEY_V2)),
        () -> Assertions.assertEquals("X123456789", checkDigit.getPatient().getKvnr()),
        () -> Assertions.assertEquals(now, checkDigit.getIatTimestamp().getTimestamp()));

    val checkDigit2 = VsdmCheckDigit.decrypt(KEY_V2, checkDigit.encrypt(KEY_V2));
    Assertions.assertAll(
        () -> Assertions.assertEquals("X123456789", checkDigit2.getPatient().getKvnr()),
        () ->
            Assertions.assertEquals(0, checkDigit2.getIatTimestamp().compareIatTimestampWith(now)));
  }

  @Test
  void shouldCreate() {
    val now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    val patient =
        new VsdmPatient(VsdmKvnr.from("X123456789"), false, LocalDate.now(), "ExampleStreet");
    val checkDigit = VsdmCheckDigitFactory.createV2(patient, 'A').setIatTimestamp(now);
    Assertions.assertAll(
        () -> Assertions.assertDoesNotThrow(() -> checkDigit.encrypt(KEY_V2)),
        () -> Assertions.assertEquals("X123456789", checkDigit.getPatient().getKvnr()),
        () -> Assertions.assertEquals(now, checkDigit.getIatTimestamp().getTimestamp()));

    val checkDigit2 = VsdmCheckDigit.decrypt(KEY_V2, checkDigit.encrypt(KEY_V2));
    Assertions.assertAll(
        () -> Assertions.assertEquals("X123456789", checkDigit2.getPatient().getKvnr()),
        () ->
            Assertions.assertEquals(0, checkDigit2.getIatTimestamp().compareIatTimestampWith(now)));
  }

  @Test
  void shouldDecryptCheckDigitV2() {
    val base64V2 = "3gyWVfvt1Yncz80adEC997AOEMJAzBxElpKwgyPfL+mGjrG31Yo4AqT9vT168v0=";
    Assertions.assertDoesNotThrow(() -> VsdmCheckDigit.decrypt(KEY_V2, base64V2));

    val checkDigit = VsdmCheckDigit.decrypt(KEY_V2, base64V2);
    Assertions.assertAll(
        () ->
            Assertions.assertEquals(
                "A123456789", checkDigit.getPatient().getKvnr(), "Kvnr is not the same"),
        () ->
            Assertions.assertFalse(
                checkDigit.getPatient().isEgkRevoked(), "EgkRevoked is not the same"),
        () ->
            Assertions.assertEquals(
                'X', checkDigit.getIdentifier().identifier(), "Identifier is not the same"),
        () ->
            Assertions.assertEquals(
                0,
                checkDigit
                    .getIatTimestamp()
                    .compareIatTimestampWith(checkDigit.getIatTimestamp().getTimestamp()),
                "IatTime is not the same"),
        () ->
            Assertions.assertEquals(
                LocalDateTime.parse("2025-01-12T21:35:12").toInstant(ZoneOffset.UTC),
                checkDigit.getIatTimestamp().getTimestamp(),
                "IatTime is not the same"));
  }

  @Test
  void shouldParseValidChecksumV1() {
    Assertions.assertDoesNotThrow(
        () ->
            VsdmCheckDigit.parse(
                "WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjkJuPVMWhLD6OBwggI="));
    val checksum =
        VsdmCheckDigit.parse("WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjkJuPVMWhLD6OBwggI=");
    Assertions.assertEquals("Y785728071", checksum.getPatient().getKvnr());
    Assertions.assertEquals(V1, checksum.getVersion());
    Assertions.assertEquals(VsdmVendorIdentifier.from('C', V1), checksum.getIdentifier());
    Assertions.assertEquals(VsdmUpdateReason.UFS_UPDATE, checksum.getUpdateReason());
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForInvalidChecksumLength() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            VsdmCheckDigit.parse(
                "WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjm49UxaEsPo4HCCAg=="));
  }

  @Test
  void testCreateV2() {
    Assertions.assertDoesNotThrow(() -> VsdmCheckDigitFactory.createV2("X123456789", 'A'));
  }

  @Test
  void testCheckDigitFactory() {
    Constructor<VsdmCheckDigitFactory> constructor;
    try {
      constructor = VsdmCheckDigitFactory.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      assertThrows(InvocationTargetException.class, constructor::newInstance);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testVsdmUtils() {
    Constructor<VsdmUtils> constructor;
    try {
      constructor = VsdmUtils.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      assertThrows(InvocationTargetException.class, constructor::newInstance);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
