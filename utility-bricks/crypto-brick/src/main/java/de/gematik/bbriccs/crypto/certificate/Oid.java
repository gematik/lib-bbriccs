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

package de.gematik.bbriccs.crypto.certificate;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.exceptions.UnsupportedOidException;
import java.util.Arrays;
import java.util.Optional;

public interface Oid {

  /**
   * Returns the OID value as String of this OID-Enumeration.
   *
   * <p>By convention, an OID implementation should have an <code>Oid</code> postfix in its name,
   * e.g. <code>ProfessionOid</code> would return the OID value as shown in the example below:
   *
   * <pre>{@code
   * String oid = ProfessionOid.OID_ARZT.getValue();
   * // oid would be "1.2.276.0.76.4
   * }</pre>
   *
   * @return the OID value as String
   * @see <a
   *     href="https://gemspec.gematik.de/downloads/gemSpec/gemSpec_OID/gemSpec_OID_V3.12.3_Aend.html">gemSpec_OID_V3.12.3_A</a>
   */
  String getValue();

  /**
   * Returns a human-readable display name for this OID.
   *
   * <p>By convention, an OID implementation should have an <code>Oid</code> postfix in its name,
   * e.g. <code>ProfessionOid</code> would return the display name as shown in the example below:
   *
   * <pre>{@code
   * String display = ProfessionOid.OID_ARZT.getDisplay();
   * // display would be "Ärztin/Arzt"
   * }</pre>
   *
   * @return the display name of the OID
   */
  String getDisplay();

  static <O extends Enum<O> & Oid> Optional<O> fromString(Class<O> oidClass, String oid) {
    return Arrays.stream(oidClass.getEnumConstants())
        .filter(it -> it.getValue().equals(oid))
        .findFirst();
  }

  static <O extends Enum<O> & Oid> O fromStringOrThrow(Class<O> oidClass, String oid) {
    return fromString(oidClass, oid)
        .orElseThrow(() -> new UnsupportedOidException(format("Unknown OID: {0}", oid)));
  }
}
