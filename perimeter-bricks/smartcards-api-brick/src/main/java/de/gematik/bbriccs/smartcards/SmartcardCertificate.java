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
import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.crypto.certificate.X509CertificateWrapper;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;
import org.bouncycastle.cert.X509CertificateHolder;

public interface SmartcardCertificate {

  X509Certificate getX509Certificate();

  X509CertificateHolder getX509CertificateHolder();

  Oid getOid();

  Supplier<InputStream> getCertificateStream();

  char[] getP12KeyStorePassword();

  KeyStore.PasswordProtection getP12KeyStoreProtection();

  X509CertificateWrapper getCertWrapper();

  PrivateKey getPrivateKey();

  CryptoSystem getCryptoSystem();
}
