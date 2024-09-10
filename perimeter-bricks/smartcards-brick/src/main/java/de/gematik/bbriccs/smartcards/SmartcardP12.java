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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import de.gematik.bbriccs.smartcards.exceptions.SmartCardKeyNotFoundException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.encoders.Base64;

@EqualsAndHashCode
@Slf4j
public abstract class SmartcardP12 implements Smartcard {

  private final SmartcardConfigDto config;
  private final List<SmartcardCertificate> certificates;
  @Getter private final String iccsn;
  @Getter private final SmartcardType type;
  @Getter private final SmartcardOwnerData ownerData;

  protected SmartcardP12(
      SmartcardType type, SmartcardConfigDto config, List<SmartcardCertificate> certificates) {
    if (!config.getType().equals(type)) {
      // prevent miss-configurations in the smartcard archive
      throw new InvalidSmartcardTypeException(type, config.getType());
    }

    this.config = config;
    this.certificates = certificates;
    this.iccsn = this.config.getIccsn();
    this.type = type;

    this.ownerData =
        LdapReader.getOwnerData(
            this.getAutCertificate().getX509Certificate().getSubjectX500Principal());

    log.trace(format("Initialize smartcard {0} with iccsn={1}", this.type.name(), this.iccsn));
  }

  @Override
  public SmartcardCertificate getAutCertificate() {
    return this.getAutCertificate(CryptoSystem.ECC_256)
        .or(() -> getAutCertificate(CryptoSystem.RSA_2048))
        .or(() -> getAutCertificate(CryptoSystem.RSA_PSS_2048))
        .orElseThrow(() -> new SmartCardKeyNotFoundException(this));
  }

  @Override
  public Optional<SmartcardCertificate> getAutCertificate(CryptoSystem cryptoSystem) {
    val oids = getAutOids();
    return oids.stream()
        .map(it -> getKey(it, cryptoSystem))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  @Override
  public PrivateKey getAuthPrivateKey() {
    return getAutCertificate().getPrivateKey();
  }

  @Override
  public PublicKey getAuthPublicKey() {
    return getAutCertificate().getX509Certificate().getPublicKey();
  }

  @Override
  @SneakyThrows
  public String getPrivateKeyBase64() {
    val pk = this.getAuthPrivateKey();

    try (val input = new ASN1InputStream(pk.getEncoded())) {
      val asn1Object = input.readObject();
      val asn1Sequence = ASN1Sequence.getInstance(asn1Object);
      val encapsulated = ASN1OctetString.getInstance(asn1Sequence.getObjectAt(2));
      val encapsulatedAsn1Sequence = ASN1Sequence.getInstance(encapsulated.getOctets());
      val encapsulatedPrivateKey =
          ASN1OctetString.getInstance(encapsulatedAsn1Sequence.getObjectAt(1));
      return Base64.toBase64String(encapsulatedPrivateKey.getOctets());
    }
  }

  protected Optional<SmartcardCertificate> getKey(Oid oid, CryptoSystem cryptoSystem) {
    log.debug(
        format("Look for smartcard certificate with oid={0} and algorithm={1}", oid, cryptoSystem));
    return certificates.stream()
        .filter(it -> it.getCryptoSystem() == cryptoSystem)
        .filter(it -> it.getOid() == oid)
        .findFirst();
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
  public String toString() {
    return format("Smartcard {0} [iccsn={1}]", type, iccsn);
  }
}
