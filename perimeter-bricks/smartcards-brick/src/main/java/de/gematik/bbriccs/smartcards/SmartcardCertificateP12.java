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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.crypto.certificate.CertificateTypeOid;
import de.gematik.bbriccs.crypto.certificate.X509CertificateWrapper;
import de.gematik.bbriccs.smartcards.exceptions.InvalidCertificateException;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.cert.X509CertificateHolder;

@EqualsAndHashCode
@Slf4j
@Getter
public class SmartcardCertificateP12 implements SmartcardCertificate {

  private final Supplier<InputStream> certificateStreamSupplier;
  private final X509CertificateWrapper certWrapper;
  private final PrivateKey privateKey;
  private final CryptoSystem cryptoSystem;

  @SneakyThrows
  public SmartcardCertificateP12(String filePath, Supplier<InputStream> certificateStreamSupplier) {
    try (val is = certificateStreamSupplier.get()) {
      val privateKeyEntry = loadEntryFromKeystore(is);

      this.certificateStreamSupplier = certificateStreamSupplier;
      this.privateKey = privateKeyEntry.getPrivateKey();
      this.certWrapper =
          new X509CertificateWrapper((X509Certificate) privateKeyEntry.getCertificate());
      this.cryptoSystem =
          CryptoSystem.fromOid(
              certWrapper.toCertificateHolder().getSignatureAlgorithm().getAlgorithm());
    } catch (Throwable t) {
      throw new InvalidCertificateException(
          format(
              "Something bad happened while loading the certificate from {0}\n{1}",
              filePath, t.getMessage()),
          t);
    }
  }

  @Override
  public X509Certificate getX509Certificate() {
    return this.certWrapper.toCertificate();
  }

  @Override
  public X509CertificateHolder getX509CertificateHolder() {
    return certWrapper.toCertificateHolder();
  }

  @Override
  public CertificateTypeOid getOid() {
    val oid = certWrapper.getCertificateTypeOid();
    return oid.orElseThrow(() -> new InvalidCertificateException(certWrapper));
  }

  @Override
  public Supplier<InputStream> getCertificateStream() {
    return certificateStreamSupplier;
  }

  @SneakyThrows
  private KeyStore.PrivateKeyEntry loadEntryFromKeystore(InputStream is) {
    val ks = KeyStore.getInstance(KeystoreType.P12.getName());
    ks.load(is, getP12KeyStorePassword());
    val alias =
        ks.aliases()
            .nextElement(); // use only the first element as each file has only a single alias
    return (KeyStore.PrivateKeyEntry)
        ks.getEntry(alias, new KeyStore.PasswordProtection(getP12KeyStorePassword()));
  }

  @Override
  public char[] getP12KeyStorePassword() {
    return "00".toCharArray();
  }

  @Override
  public KeyStore.PasswordProtection getP12KeyStoreProtection() {
    return new KeyStore.PasswordProtection(this.getP12KeyStorePassword());
  }

  @Override
  public String toString() {
    return format("SmartcardKey with algorithm={0}", cryptoSystem);
  }
}
