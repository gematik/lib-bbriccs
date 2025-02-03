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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class VsdmPatient {
  // Example 2018-01-11T07:00:00
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final VsdmKvnr kvnr;
  @Getter private final boolean isEgkRevoked;
  @Getter private final LocalDate insuranceStartDate;
  @Getter private final String street;

  public VsdmPatient(VsdmKvnr kvnr) {
    this(kvnr, LocalDate.now());
  }

  public VsdmPatient(VsdmKvnr kvnr, LocalDate insuranceStartDate) {
    this(kvnr, false, insuranceStartDate, "");
  }

  public String getKvnr() {
    return kvnr.kvnr();
  }

  public byte[] getKvnrAsByteArray() {
    return kvnr.generate();
  }

  /**
   * Requirement: A_27278 ; Feld_1
   *
   * @return - The generated hash check value as a byte array with a length of 5.
   */
  public byte[] generateField1() {
    val hash = generateHash(insuranceStartDate, street);
    // Determine S based on eGK status
    byte s = isEgkRevoked ? (byte) 128 : 0;
    hash[0] = (byte) (hash[0] | s);
    return hash;
  }

  /**
   * Requirement: A_27352
   *
   * @param insuranceStartDate - the insurance start date
   * @param street - the address/street
   * @return - The generated hash check value as a byte array with a length of 5.
   */
  @SneakyThrows
  public static byte[] generateHash(LocalDate insuranceStartDate, String street) {
    Objects.requireNonNull(insuranceStartDate, "Insurance start date is required");
    Objects.requireNonNull(street, "Street is required");

    val digest = MessageDigest.getInstance("SHA-256");

    val insuranceStartDateAsByteArray =
        DATE_FORMATTER.format(insuranceStartDate).getBytes(StandardCharsets.UTF_8);
    val streetAsByteArray = street.trim().getBytes(Charset.forName("ISO-8859-15"));
    val plain = new byte[insuranceStartDateAsByteArray.length + streetAsByteArray.length];
    System.arraycopy(
        insuranceStartDateAsByteArray, 0, plain, 0, insuranceStartDateAsByteArray.length);
    System.arraycopy(
        streetAsByteArray,
        0,
        plain,
        insuranceStartDateAsByteArray.length,
        streetAsByteArray.length);

    val hash = digest.digest(plain);
    val hash40 = Arrays.copyOfRange(hash, 0, 5);
    // Set the MSB of the first byte to 0
    hash40[0] &= 0x7F;
    return hash40;
  }

  public static VsdmPatient parse(byte[] data, VsdmCheckDigitVersion version) {
    val kvnr = VsdmKvnr.parse(data, version);
    if (version == VsdmCheckDigitVersion.V1) {
      return new VsdmPatient(kvnr);
    }
    boolean isEgkRevoked = (data[0] & 0x80) != 0;
    return new VsdmPatient(kvnr, isEgkRevoked, null, null);
  }
}
