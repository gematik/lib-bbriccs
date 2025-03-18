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

package de.gematik.bbriccs.crypto;

import static java.text.MessageFormat.format;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_RSASSA_PSS;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.sha256WithRSAEncryption;
import static org.bouncycastle.asn1.x9.X9ObjectIdentifiers.ecdsa_with_SHA256;

import de.gematik.bbriccs.crypto.exceptions.InvalidCryptographySpecificationException;
import de.gematik.bbriccs.crypto.exceptions.UnsupportedCryptographySystemException;
import java.util.Arrays;
import lombok.Getter;
import lombok.val;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@Getter
public enum CryptoSystem {
  RSA_2048("RSA", CryptographySpecification.RSA, sha256WithRSAEncryption, 2048),
  RSA_PSS_2048("RSASSA-PSS", CryptographySpecification.RSA, id_RSASSA_PSS, 2048),
  ECC_256("ECC", CryptographySpecification.ECC, ecdsa_with_SHA256, 256);

  public static final CryptoSystem DEFAULT_CRYPTO_SYSTEM = ECC_256;

  private final String name;
  private final int keyLength;
  private final ASN1ObjectIdentifier oid;
  private final CryptographySpecification specification;

  CryptoSystem(
      String name, CryptographySpecification standard, ASN1ObjectIdentifier oid, int keyLength) {
    this.name = name;
    this.oid = oid;
    this.keyLength = keyLength;
    this.specification = standard;
  }

  public static CryptoSystem fromString(String value) {
    return switch (value.toUpperCase().replace("-", "_")) {
      case "RSA_2048", "R2048", "RSA" -> RSA_2048;
      case "RSASSA_PSS" -> RSA_PSS_2048;
      case "E256", "ECC_256", "ECC", "RSA_ECC" -> ECC_256;
      default -> throw new UnsupportedCryptographySystemException(
          format("Cryptography {0} is not supported", value));
    };
  }

  public static CryptoSystem fromOid(ASN1ObjectIdentifier oid) {
    return Arrays.stream(CryptoSystem.values())
        .filter(it -> it.oid.equals(oid))
        .findFirst()
        .orElseThrow(
            () ->
                new UnsupportedCryptographySystemException(
                    format("Cryptography Oid {0} is not supported", oid)));
  }

  public static CryptoSystem fromSpecificationUrn(String urn) {
    val standard = CryptographySpecification.fromUrn(urn);
    return Arrays.stream(CryptoSystem.values())
        .filter(it -> it.getSpecification().equals(standard))
        .findFirst()
        .orElseThrow(() -> new InvalidCryptographySpecificationException(urn));
  }

  @Override
  public String toString() {
    return this.name + " " + this.keyLength;
  }
}
