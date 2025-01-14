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

package de.gematik.bbriccs.vsdm.types;

import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.bbriccs.vsdm.VsdmUtils;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class VsdmIssuedAtTimestamp {
  // Anforderung A_27323
  private static final Instant OFFSET_V2 =
      LocalDateTime.parse("2025-01-01T00:00:00").toInstant(ZoneOffset.UTC);

  private final Instant timestamp;
  private final VsdmCheckDigitVersion version;

  public VsdmIssuedAtTimestamp(Instant iatTimestamp, VsdmCheckDigitVersion version) {
    this.timestamp = iatTimestamp;
    this.version = version;
  }

  public VsdmIssuedAtTimestamp(VsdmCheckDigitVersion version) {
    this(LocalDateTime.now().toInstant(ZoneOffset.UTC), version);
  }

  public byte[] generate() {
    if (version == VsdmCheckDigitVersion.V1) {
      val timestampAsLong = this.getTimestamp().getEpochSecond();
      return ("" + timestampAsLong).getBytes(StandardCharsets.UTF_8);
    } else {
      // Requirement: A_27278 ; r_iat_8
      long relativeTimestamp = timestamp.getEpochSecond() - OFFSET_V2.getEpochSecond();
      relativeTimestamp >>= 3;
      return VsdmUtils.long2ByteArray(relativeTimestamp, 3);
    }
  }

  public static VsdmIssuedAtTimestamp parse(byte[] data, VsdmCheckDigitVersion version) {
    if (version == VsdmCheckDigitVersion.V1) {
      val timestamp =
          Instant.ofEpochSecond(
              Long.parseLong(
                  new String(VsdmUtils.copyByteArrayFrom(data, 10, 20), StandardCharsets.UTF_8)));
      return new VsdmIssuedAtTimestamp(timestamp.truncatedTo(ChronoUnit.SECONDS), version);
    } else {
      val iat = VsdmUtils.copyByteArrayFrom(data, 5, 8);
      long relativeTime =
          (((iat[0] & 0xFF) << 16)
                  | // Höchstwertiges Byte
                  ((iat[1] & 0xFF) << 8)
                  | // Mittleres Byte
                  (iat[2] & 0xFF))
              << 3;
      val timestamp = Instant.ofEpochSecond(relativeTime + OFFSET_V2.getEpochSecond());
      return new VsdmIssuedAtTimestamp(timestamp.truncatedTo(ChronoUnit.SECONDS), version);
    }
  }

  public int compareIatTimestampWith(Instant referenceTime) {
    // Anforderung A_27279
    long iatTimeInSeconds = timestamp.getEpochSecond();
    long referenceTimeInSeconds = referenceTime.getEpochSecond();
    if (referenceTimeInSeconds - 20 * 60 + 30 > iatTimeInSeconds) {
      return -1;
    }
    if (referenceTimeInSeconds + 30 < iatTimeInSeconds) {
      return 1;
    }
    return 0;
  }
}
