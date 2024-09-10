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

package de.gematik.bbriccs.konnektor;

import de.gematik.bbriccs.konnektor.utils.BNetzAVLCa;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class SoftKonVerifier {

  public boolean verify(byte[] input) {
    var isCompletelyValid = true;
    try {
      val cv = new CommonCertificateVerifier();
      cv.setOcspSource(new OnlineOCSPSource());

      val trustedCertSource = new CommonTrustedCertificateSource();
      for (BNetzAVLCa ca : BNetzAVLCa.values()) {
        trustedCertSource.addCertificate(new CertificateToken(ca.getCertificate()));
      }
      cv.setTrustedCertSources(trustedCertSource);
      val documentValidator = SignedDocumentValidator.fromDocument(new InMemoryDocument(input));
      documentValidator.setCertificateVerifier(cv);
      val reports = documentValidator.validateDocument();
      val report = reports.getSimpleReport();
      val signatures = documentValidator.getSignatures();

      for (val signature : signatures) {
        // There are some certificates (e.g. RSA/ECC QES from  adelheid ulmenwald) with OCSP status
        // unknown
        // For this, the validation policy needs to be adjusted, or it needs to be checked why the
        // status is unknown
        val signatureIsValid =
            report.isValid(signature.getId())
                || report.getIndication(signature.getId()) == Indication.INDETERMINATE;
        isCompletelyValid = isCompletelyValid && signatureIsValid;
        val signingCertToken = signature.getSigningCertificateToken();
        val caToken = new CertificateToken(BNetzAVLCa.getCA(signingCertToken.getCertificate()));
        val ocsp = signature.getOCSPSource().getRevocationToken(signingCertToken, caToken);
        if (ocsp != null) {
          log.info(
              "Ocsp Status for signing certificate with {} is {}",
              signingCertToken.getSubject().getCanonical(),
              ocsp.getStatus());
        }
        log.info(
            "CAdES signature signed by {} is {}valid",
            report.getSignedBy(signature.getId()),
            !signatureIsValid ? "in" : "");
      }
    } catch (Throwable t) {
      isCompletelyValid = false;
      log.warn("Failed to verify with a certificate exception", t);
    }

    return isCompletelyValid;
  }
}
