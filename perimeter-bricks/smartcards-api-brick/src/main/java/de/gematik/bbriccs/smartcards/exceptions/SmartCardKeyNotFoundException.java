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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.bbriccs.smartcards.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.CertificateTypeOid;
import de.gematik.bbriccs.smartcards.Smartcard;
import java.util.List;

public class SmartCardKeyNotFoundException extends RuntimeException {

  public SmartCardKeyNotFoundException(
      Smartcard smartcard, List<CertificateTypeOid> oids, CryptoSystem cryptoSystem) {
    super(
        format("Key with {0} and Algorithm {1} not found for {2}", oids, cryptoSystem, smartcard));
  }

  public SmartCardKeyNotFoundException(Smartcard smartcard) {
    this(smartcard, smartcard.getAutOids());
  }

  public SmartCardKeyNotFoundException(Smartcard smartcard, List<CertificateTypeOid> oids) {
    super(format("Key with {0} not found for {1}", oids, smartcard));
  }

  public SmartCardKeyNotFoundException(
      Smartcard smartcard, CertificateTypeOid oid, CryptoSystem crypto) {
    this(smartcard, List.of(oid), crypto);
  }
}
