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

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SmcBP12 extends InstituteSmartcardP12 implements SmcB {

  public SmcBP12(SmartcardConfigDto config, List<SmartcardCertificate> certificates) {
    super(SmartcardType.SMC_B, config, certificates);
  }

  @Override
  public SmartcardCertificate getOSigCertificate(CryptoSystem cryptoSystem) {
    return getKey(Oid.OID_SMC_B_OSIG, cryptoSystem)
        .orElseThrow(
            () -> new SmartCardKeyNotFoundException(this, Oid.OID_SMC_B_OSIG, cryptoSystem));
  }

  @Override
  public List<Oid> getAutOids() {
    return List.of(Oid.OID_SMC_B_AUT);
  }

  @Override
  public Oid getEncOid() {
    return Oid.OID_SMC_B_ENC;
  }
}
