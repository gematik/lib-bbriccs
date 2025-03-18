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

package de.gematik.bbriccs.smartcards;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import java.util.List;
import lombok.val;

public abstract class InstituteSmartcardP12 extends SmartcardP12 implements InstituteSmartcard {

  protected InstituteSmartcardP12(
      SmartcardType type, SmartcardConfigDto config, List<SmartcardCertificate> certificates) {
    super(type, config, certificates);
  }

  @Override
  public String getTelematikId() {
    return getAutCertificate()
        .getCertWrapper()
        .getProfessionId()
        .orElse(this.getOwnerData().getOrganization());
  }

  @Override
  public SmartcardCertificate getEncCertificate(CryptoSystem cryptoSystem) {
    val oid = getEncOid();
    return getKey(oid, cryptoSystem)
        .orElseThrow(() -> new SmartCardKeyNotFoundException(this, oid, cryptoSystem));
  }
}
