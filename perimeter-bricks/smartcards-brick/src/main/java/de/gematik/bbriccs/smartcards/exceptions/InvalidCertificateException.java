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

package de.gematik.bbriccs.smartcards.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.certificate.X509CertificateWrapper;

public class InvalidCertificateException extends RuntimeException {
  public InvalidCertificateException(X509CertificateWrapper cert) {
    this(format("Certificate {0} is not supported", cert.toCertificateHolder().getSubject()));
  }

  public InvalidCertificateException(String message) {
    super(message);
  }

  public InvalidCertificateException(String message, Throwable cause) {
    super(message, cause);
  }
}