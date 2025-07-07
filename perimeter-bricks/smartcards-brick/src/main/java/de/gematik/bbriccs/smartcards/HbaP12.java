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

package de.gematik.bbriccs.smartcards;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.CertificateTypeOid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class HbaP12 extends InstituteSmartcardP12 implements Hba {

  public HbaP12(SmartcardConfigDto config, List<SmartcardCertificate> certificates) {
    super(SmartcardType.HBA, config, certificates);
  }

  @Override
  public SmartcardCertificate getQesCertificate(CryptoSystem cryptoSystem) {
    return getKey(CertificateTypeOid.OID_HBA_QES, cryptoSystem)
        .orElseThrow(
            () ->
                new SmartCardKeyNotFoundException(
                    this, CertificateTypeOid.OID_HBA_QES, cryptoSystem));
  }

  @Override
  public List<CertificateTypeOid> getAutOids() {
    return List.of(CertificateTypeOid.OID_HBA_AUT);
  }

  @Override
  public CertificateTypeOid getEncOid() {
    return CertificateTypeOid.OID_HBA_ENC;
  }
}
