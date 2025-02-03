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

package de.gematik.bbriccs.smartcards;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.InvalidCertificateException;
import de.gematik.bbriccs.smartcards.exceptions.MissingCardAttribute;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class EgkP12 extends SmartcardP12 implements Egk {

  private static final Pattern KVNR_PATTERN = Pattern.compile("^([A-Z]\\d{9})$");
  private final String kvnr;

  public EgkP12(SmartcardConfigDto config, List<SmartcardCertificate> certificates) {
    super(SmartcardType.EGK, config, certificates);
    this.kvnr =
        this.getOwnerData().getOrganizationUnit().stream()
            .filter(ou -> KVNR_PATTERN.matcher(ou).matches())
            .findFirst()
            .orElseThrow(
                () ->
                    new InvalidCertificateException(
                        format(
                            "Authorization Certificate for eGK {0} is missing a valid KVNR",
                            this.getIccsn())));
  }

  @Override
  public List<Oid> getAutOids() {
    return List.of(Oid.OID_EGK_AUT, Oid.OID_EGK_AUT_ALT);
  }

  @Override
  public LocalDate getInsuranceStartDate() {
    // Normally, the date is read from the common insured data of the Egk. The issue date of the AUT
    // certificate is a work a round.
    return getAutCertificate(CryptoSystem.ECC_256)
        .or(() -> getAutCertificate(CryptoSystem.RSA_2048))
        .map(it -> it.getX509Certificate().getNotBefore().toInstant())
        .map(it -> LocalDate.ofEpochDay(it.getEpochSecond()))
        .orElseThrow(() -> new MissingCardAttribute(this, "Insurance Start Date"));
  }
}
