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

import de.gematik.bbriccs.crypto.exceptions.InvalidCryptographySpecificationException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CryptographySpecification {
  RSA("urn:ietf:rfc:3447"),
  ECC("urn:bsi:tr:03111:ecdsa");

  private final String urn;

  public static CryptographySpecification fromUrn(String urn) {
    return Arrays.stream(values())
        .filter(it -> it.getUrn().equals(urn))
        .findFirst()
        .orElseThrow(() -> new InvalidCryptographySpecificationException(urn));
  }
}