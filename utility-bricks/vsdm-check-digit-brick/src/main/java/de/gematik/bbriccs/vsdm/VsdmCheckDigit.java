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

import de.gematik.bbriccs.crypto.encryption.AesGcm;
import de.gematik.bbriccs.vsdm.exceptions.ParsingVersionException;
import de.gematik.bbriccs.vsdm.types.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;

@Slf4j
public class VsdmCheckDigit implements VsdmCheckDigitV1, VsdmCheckDigitV2 {

  @Getter private final VsdmPatient patient;
  @Getter protected final VsdmVendorIdentifier identifier;
  @Getter private final VsdmCheckDigitVersion version;
  @Getter private VsdmIssuedAtTimestamp iatTimestamp;

  private VsdmUpdateReason updateReason = VsdmUpdateReason.UFS_UPDATE;

  protected VsdmCheckDigit(VsdmPatient patient, VsdmCheckDigitVersion version) {
    this(patient, null, version);
  }

  protected VsdmCheckDigit(VsdmPatient patient, VsdmVendorIdentifier identifier) {
    this(patient, identifier, identifier.version());
  }

  protected VsdmCheckDigit(
      VsdmPatient patient, VsdmVendorIdentifier identifier, VsdmCheckDigitVersion version) {
    this.patient = patient;
    this.identifier = identifier;
    this.version = version;
    this.iatTimestamp = new VsdmIssuedAtTimestamp(version);
  }

  private static byte[] base64Decode(String base64) {
    val decodedBase64 = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
    if (decodedBase64.length != 47) {
      throw new IllegalArgumentException("Invalid checksum length");
    }
    return decodedBase64;
  }

  /**
   * Requirement: A_27278
   *
   * @param key the key to encrypt the check digit
   * @return a base64 encoded check digit
   */
  @SneakyThrows
  public String encrypt(VsdmKey key) {
    checkVsdmVersion(VsdmCheckDigitVersion.V2, version);

    val plain =
        concatenate(
            List.of(patient.generateField1(), iatTimestamp.generate(), patient.generateKvnr()));
    val aesGcm = new AesGcm(12, 16);
    val encrypt = aesGcm.encrypt(key.getKeyForVersion2(), plain);

    byte header = (byte) (128 + identifier.generate() + key.keyVersion().generate());
    return Base64.getEncoder().encodeToString(concatenate(List.of(header, encrypt)));
  }

  /**
   * Requirement: A_27278
   *
   * @param key the key to decrypt the check digit
   * @param base64 the base64 encoded check digit
   * @return a decrypted check digit
   */
  public static VsdmCheckDigitV2 decrypt(VsdmKey key, String base64) {
    val version = VsdmCheckDigitVersion.fromData(base64);
    checkVsdmVersion(VsdmCheckDigitVersion.V2, version);

    val decodedBase64 = base64Decode(base64);

    val ciphertext = VsdmUtils.copyByteArrayFrom(decodedBase64, 1, decodedBase64.length);
    val aesGcm = new AesGcm(12, 16);
    val plain = aesGcm.decrypt(key.getKeyForVersion2(), ciphertext);
    return new VsdmCheckDigit(
            VsdmPatient.parse(plain, version),
            VsdmVendorIdentifier.parseV2(decodedBase64, key.keyVersion()))
        .setIatTimestamp(VsdmIssuedAtTimestamp.parse(plain, version));
  }

  public static VsdmCheckDigitV1 parse(String checksumAsBase64) {
    val version = VsdmCheckDigitVersion.fromData(checksumAsBase64);
    checkVsdmVersion(VsdmCheckDigitVersion.V1, version);

    val decodedBase64 = base64Decode(checksumAsBase64);

    val patient = VsdmPatient.parse(decodedBase64, version);
    val iatTimestamp = VsdmIssuedAtTimestamp.parse(decodedBase64, version);
    val reason = VsdmUpdateReason.fromChecksum((char) decodedBase64[20]);
    val identifier = VsdmVendorIdentifier.parseV1(decodedBase64);

    return new VsdmCheckDigit(patient, identifier, version)
        .setIatTimestamp(iatTimestamp)
        .setUpdateReason(reason);
  }

  /**
   * The method generate a checksum encode as base64. The checksum contains the first 24 bytes of
   * the signature, which contains a HMac hash (SHA256) over the fields 1 to 5
   *
   * @return a base64 encoded checksum
   */
  public String sign(VsdmKey key) {
    checkVsdmVersion(VsdmCheckDigitVersion.V1, version);

    val hMac = new HMac(new SHA256Digest());
    hMac.init(key.getKeyForVersion1());

    val data =
        concatenate(
            List.of(
                patient.generateKvnr(),
                iatTimestamp.generate(),
                updateReason.generate(),
                identifier.generate(),
                key.keyVersion().generate()));

    hMac.update(data, 0, data.length);
    val signature = new byte[hMac.getMacSize()];
    hMac.doFinal(signature, 0);

    val checksum = new byte[data.length + 24];
    System.arraycopy(data, 0, checksum, 0, data.length);
    System.arraycopy(signature, 0, checksum, data.length, 24);
    return Base64.getEncoder().encodeToString(checksum);
  }

  public VsdmUpdateReason getUpdateReason() {
    checkVsdmVersion(VsdmCheckDigitVersion.V1, version);
    return updateReason;
  }

  public VsdmCheckDigit setIatTimestamp(Instant iatTimestamp) {
    return setIatTimestamp(
        new VsdmIssuedAtTimestamp(iatTimestamp.truncatedTo(ChronoUnit.SECONDS), version));
  }

  public VsdmCheckDigit setIatTimestamp(VsdmIssuedAtTimestamp iatTimestamp) {
    this.iatTimestamp = iatTimestamp;
    return this;
  }

  public VsdmCheckDigit setUpdateReason(VsdmUpdateReason reason) {
    checkVsdmVersion(VsdmCheckDigitVersion.V1, version);
    this.updateReason = reason;
    return this;
  }

  private byte[] concatenate(List<Object> elements) {
    int totalLength = 0;
    for (Object element : elements) {
      if (element instanceof byte[] elementAsByteArray) {
        totalLength += elementAsByteArray.length;
      } else if (element instanceof Byte) {
        totalLength += 1;
      } else {
        throw new IllegalArgumentException("Unsupported element type");
      }
    }

    byte[] result = new byte[totalLength];
    int currentPosition = 0;

    for (Object element : elements) {
      if (element instanceof byte[] byteArray) {
        System.arraycopy(byteArray, 0, result, currentPosition, byteArray.length);
        currentPosition += byteArray.length;
      } else if (element instanceof Byte asByte) {
        result[currentPosition] = asByte;
        currentPosition += 1;
      }
    }
    return result;
  }

  private static void checkVsdmVersion(
      VsdmCheckDigitVersion supportedVersion, VsdmCheckDigitVersion version) {
    if (supportedVersion != version) {
      throw new ParsingVersionException(supportedVersion, version);
    }
  }
}
