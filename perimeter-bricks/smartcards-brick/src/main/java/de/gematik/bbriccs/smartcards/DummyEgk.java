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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.CertificateTypeOid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is a dummy eGK required for the use-cases where we only have the KVNR and no private keys
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DummyEgk implements Egk {

  private final SmartcardConfigDto config;
  private final SmartcardOwnerData ownerData;

  public static DummyEgk fromConfig(SmartcardConfigDto config) {
    val ownerData = SmartcardOwnerData.builder().commonName(config.getOwnerName()).build();
    return new DummyEgk(config, ownerData);
  }

  @Override
  public SmartcardCertificate getAutCertificate() {
    return this.getAutCertificate(CryptoSystem.DEFAULT_CRYPTO_SYSTEM).orElse(null);
  }

  @Override
  public Optional<SmartcardCertificate> getAutCertificate(CryptoSystem cryptoSystem) {
    return Optional.empty();
  }

  @Override
  public PrivateKey getAuthPrivateKey() {
    return null;
  }

  @Override
  public PublicKey getAuthPublicKey() {
    return null;
  }

  @Override
  public String getPrivateKeyBase64() {
    return "";
  }

  @Override
  public List<CertificateTypeOid> getAutOids() {
    return List.of(CertificateTypeOid.OID_EGK_AUT, CertificateTypeOid.OID_EGK_AUT_ALT);
  }

  @Override
  public String getIccsn() {
    return this.config.getIccsn();
  }

  @Override
  public SmartcardType getType() {
    return SmartcardType.EGK;
  }

  @Override
  public SmartcardOwnerData getOwnerData() {
    return this.ownerData;
  }

  @Override
  public Map<String, Object> getExtension() {
    return this.config.getSmartcardExtension();
  }

  @Override
  public <E extends SmartcardExtension> E getExtensionAs(Class<E> extensionType) {
    return new ObjectMapper().convertValue(this.getExtension(), extensionType);
  }

  @Override
  public String getKvnr() {
    return this.config.getIdentifier();
  }

  @Override
  public LocalDate getInsuranceStartDate() {
    return LocalDate.now();
  }
}
