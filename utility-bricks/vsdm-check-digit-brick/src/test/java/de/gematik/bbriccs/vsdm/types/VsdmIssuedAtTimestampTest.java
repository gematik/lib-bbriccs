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

package de.gematik.bbriccs.vsdm.types;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.exceptions.ParsingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.val;
import org.junit.jupiter.api.Test;

class VsdmIssuedAtTimestampTest {

  @Test
  void shouldGenerateCorrectBytesForV1Version() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(Instant.ofEpochSecond(1609459200), VsdmCheckDigitVersion.V1);
    byte[] result = vsdmIssuedAtTimestamp.generate();
    assertArrayEquals("1609459200".getBytes(StandardCharsets.UTF_8), result);
  }

  @Test
  void shouldGenerateCorrectBytesForV2Version() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(Instant.parse("2025-01-01T00:00:00Z"), VsdmCheckDigitVersion.V2);
    byte[] result = vsdmIssuedAtTimestamp.generate();
    assertArrayEquals(new byte[] {0x00, 0x00, 0x00}, result);
  }

  @Test
  void shouldParseCorrectTimestampForV1Version() {
    byte[] data = "00000000001609459200".getBytes(StandardCharsets.UTF_8);
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        VsdmIssuedAtTimestamp.parse(data, VsdmCheckDigitVersion.V1);
    assertEquals(Instant.ofEpochSecond(1609459200), vsdmIssuedAtTimestamp.getTimestamp());
  }

  @Test
  void shouldParseCorrectTimestampForV2Version() {
    val issued =
        new VsdmIssuedAtTimestamp(Instant.parse("2025-01-01T00:00:00Z"), VsdmCheckDigitVersion.V2);
    byte[] issuedBytes = issued.generate();
    byte[] data =
        new byte[] {
          issuedBytes[0],
          issuedBytes[0],
          issuedBytes[0],
          issuedBytes[0],
          issuedBytes[0],
          issuedBytes[1],
          issuedBytes[2],
          issuedBytes[0]
        };
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        VsdmIssuedAtTimestamp.parse(data, VsdmCheckDigitVersion.V2);
    assertEquals(Instant.parse("2025-01-01T00:00:00Z"), vsdmIssuedAtTimestamp.getTimestamp());
  }

  @Test
  void shouldThrowParsingExceptionForInvalidDataV1() {
    byte[] data = new byte[] {0x00};
    assertThrows(
        ParsingException.class, () -> VsdmIssuedAtTimestamp.parse(data, VsdmCheckDigitVersion.V1));
  }

  @Test
  void shouldThrowParsingExceptionForInvalidDataV2() {
    byte[] data = new byte[] {0x00};
    assertThrows(
        ParsingException.class, () -> VsdmIssuedAtTimestamp.parse(data, VsdmCheckDigitVersion.V2));
  }

  @Test
  void shouldReturnNegativeOneWhenTimestampIsTooOld() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(
            Instant.now().minus(21, ChronoUnit.MINUTES), VsdmCheckDigitVersion.V2);
    assertEquals(-1, vsdmIssuedAtTimestamp.compareIatTimestampWith(Instant.now()));
  }

  @Test
  void shouldReturnOneWhenTimestampIsInTheFuture() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(
            Instant.now().plus(1, ChronoUnit.MINUTES), VsdmCheckDigitVersion.V2);
    assertEquals(1, vsdmIssuedAtTimestamp.compareIatTimestampWith(Instant.now()));
  }

  @Test
  void shouldReturnZeroWhenTimestampIsWithinValidRange() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(Instant.now(), VsdmCheckDigitVersion.V2);
    assertEquals(0, vsdmIssuedAtTimestamp.compareIatTimestampWith(Instant.now()));
  }

  @Test
  void shouldNotThrownNullpointer() {
    assertDoesNotThrow(() -> new VsdmIssuedAtTimestamp(VsdmCheckDigitVersion.V2));
  }

  @Test
  void shouldThrowExceptionForNullDataInParse() {
    assertThrows(
        ParsingException.class, () -> VsdmIssuedAtTimestamp.parse(null, VsdmCheckDigitVersion.V1));
  }

  @Test
  void shouldReturnNegativeOneWhenTimestampIsExactly21MinutesOld() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(
            Instant.now().minus(21, ChronoUnit.MINUTES), VsdmCheckDigitVersion.V1);
    assertEquals(-1, vsdmIssuedAtTimestamp.compareIatTimestampWith(Instant.now()));
  }

  @Test
  void shouldReturnOneWhenTimestampIsExactlyOneMinuteInTheFuture() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(
            Instant.now().plus(1, ChronoUnit.MINUTES), VsdmCheckDigitVersion.V1);
    assertEquals(1, vsdmIssuedAtTimestamp.compareIatTimestampWith(Instant.now()));
  }

  @Test
  void shouldReturnZeroWhenTimestampIsExactlyNow() {
    VsdmIssuedAtTimestamp vsdmIssuedAtTimestamp =
        new VsdmIssuedAtTimestamp(Instant.now(), VsdmCheckDigitVersion.V2);
    assertEquals(0, vsdmIssuedAtTimestamp.compareIatTimestampWith(Instant.now()));
  }
}
