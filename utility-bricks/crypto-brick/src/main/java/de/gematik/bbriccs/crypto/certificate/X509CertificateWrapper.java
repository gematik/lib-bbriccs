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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.*;
import java.util.*;
import javax.naming.ldap.LdapName;
import lombok.*;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.isismtt.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.*;
import org.bouncycastle.cert.jcajce.*;

public class X509CertificateWrapper {
  private final X509CertificateHolder certHolder;
  private final X509Certificate cert;

  @SneakyThrows
  public X509CertificateWrapper(X509Certificate cert) {
    this.cert = cert;
    this.certHolder = new X509CertificateHolder(cert.getEncoded());
  }

  @SneakyThrows
  public static X509CertificateWrapper fromPem(String pem) {
    val certificateFactory = CertificateFactory.getInstance("X.509");
    val cert =
        (X509Certificate)
            certificateFactory.generateCertificate(
                new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
    return new X509CertificateWrapper(cert);
  }

  @SneakyThrows
  public Optional<String> getIssuerCN() {
    val issuerX500Principal = cert.getIssuerX500Principal().getName();
    val ldapDN = new LdapName(issuerX500Principal);
    return ldapDN.getRdns().stream()
        .filter(it -> it.getType().equalsIgnoreCase("CN"))
        .map(it -> it.getValue().toString())
        .findFirst();
  }

  private <T extends ASN1Encodable> Optional<T> getAsn1ElementByType(
      Iterator<ASN1Encodable> iter, Class<T> type) {
    while (iter.hasNext()) {
      val element = iter.next();
      if (type.isInstance(element)) {
        return Optional.of(type.cast(element));
      } else if (element instanceof ASN1Sequence seq) {
        val ret = getAsn1ElementByType((seq).iterator(), type);
        if (ret.isPresent()) {
          return ret;
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> getTelematikId() {
    val valueIsIsMttAdmission =
        (ASN1Sequence)
            certHolder
                .getExtension(ISISMTTObjectIdentifiers.id_isismtt_at_admission)
                .getParsedValue();
    return getAsn1ElementByType(valueIsIsMttAdmission.iterator(), ASN1PrintableString.class)
        .map(ASN1PrintableString::getString);
  }

  public Optional<ProfessionOid> getProfessionId() {
    val valueIsIsMttAdmission =
        (ASN1Sequence)
            certHolder
                .getExtension(ISISMTTObjectIdentifiers.id_isismtt_at_admission)
                .getParsedValue();

    return getAsn1ElementByType(valueIsIsMttAdmission.iterator(), ASN1ObjectIdentifier.class)
        .flatMap(it -> ProfessionOid.fromString(it.getId()));
  }

  public boolean isRsaEncryption() {
    return certHolder
        .getSubjectPublicKeyInfo()
        .getAlgorithm()
        .getAlgorithm()
        .getId()
        .equals(PKCSObjectIdentifiers.rsaEncryption.getId());
  }

  @SneakyThrows
  public Optional<CertificateTypeOid> getCertificateTypeOid() {
    val policies =
        CertificatePolicies.getInstance(
            JcaX509ExtensionUtils.parseExtensionValue(cert.getExtensionValue("2.5.29.32")));
    return Arrays.stream(policies.getPolicyInformation())
        .map(p -> p.getPolicyIdentifier().toString())
        .map(CertificateTypeOid::getByOid)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  public X509CertificateHolder toCertificateHolder() {
    return certHolder;
  }

  public X509Certificate toCertificate() {
    return cert;
  }
}
