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

package de.gematik.bbriccs.konnektor;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.exceptions.SmartcardException;
import de.gematik.bbriccs.konnektor.utils.BNetzAVLCa;
import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.bbriccs.smartcards.SmartcardCertificate;
import de.gematik.bbriccs.smartcards.SmcB;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.cades.signature.CMSSignedDocument;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.CMSSignedDataBuilder;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.util.encoders.Base64;

@Slf4j
public class SoftKonSigner {

  private final CAdESService cades;
  private final CommonCertificateVerifier certVerifier;

  public SoftKonSigner() {
    this.certVerifier = new CommonCertificateVerifier();
    this.cades = new CAdESService(certVerifier);
  }

  private static EncryptionAlgorithm getEncryptionAlgorithm(SmartcardCertificate certificate) {
    val ret =
        switch (certificate.getCryptoSystem()) {
          case RSA_2048, RSA_PSS_2048 -> EncryptionAlgorithm.RSA;
          case ECC_256 -> EncryptionAlgorithm.ECDSA;
        };

    log.trace("Encryption Algorithm for signing from {} to {}", certificate.getCryptoSystem(), ret);
    return ret;
  }

  public byte[] signDocument(
      Smartcard smartcard, CryptoSystem cryptoSystem, boolean includeRevocationInfo, byte[] data) {
    if (smartcard instanceof Hba hba) {
      return signDocument(hba, cryptoSystem, includeRevocationInfo, data);
    } else if (smartcard instanceof SmcB smcB) {
      return signDocument(smcB, cryptoSystem, includeRevocationInfo, data);
    } else {
      throw new SmartcardException(
          format(
              "Smartcard {0} ({1}) is not supported for Signing a document",
              smartcard.getType(), smartcard.getClass().getSimpleName()));
    }
  }

  /**
   * Using an HBA to sign a document will result in a QES signature
   *
   * @param hba to be used for the QES
   * @param cryptoSystem to be used for the QES which can be RSA or ECC
   * @param includeRevocationInfo if set to true will include OCSP-Response to the signature
   * @param data to be signed
   */
  public byte[] signDocument(
      Hba hba, CryptoSystem cryptoSystem, boolean includeRevocationInfo, String data) {
    return signDocument(
        hba, cryptoSystem, includeRevocationInfo, data.getBytes(StandardCharsets.UTF_8));
  }

  public byte[] signDocument(
      Hba hba, CryptoSystem cryptoSystem, boolean includeRevocationInfo, byte[] data) {
    val certificate = hba.getQesCertificate(cryptoSystem);
    return signDocument(certificate, includeRevocationInfo, data);
  }

  /**
   * Using an SMC-B to sign a document will result in a non-QES signature
   *
   * @param smcB to be used for the non-QES
   * @param cryptoSystem to be used for the non-QES which can be RSA or ECC
   * @param includeRevocationInfo if set to true will include OCSP-Response to the signature
   * @param data to be signed
   */
  public byte[] signDocument(
      SmcB smcB, CryptoSystem cryptoSystem, boolean includeRevocationInfo, String data) {
    return signDocument(
        smcB, cryptoSystem, includeRevocationInfo, data.getBytes(StandardCharsets.UTF_8));
  }

  public byte[] signDocument(
      SmcB smcB, CryptoSystem cryptoSystem, boolean includeRevocationInfo, byte[] data) {
    val certificate = smcB.getOSigCertificate(cryptoSystem);
    return signDocument(certificate, includeRevocationInfo, data);
  }

  @SneakyThrows
  private byte[] signDocument(
      SmartcardCertificate certificate, boolean includeRevocationInfo, byte[] data) {
    val signingDate = new Date();
    val mimeType = MimeTypeEnum.XML; // Note: only XML for now!
    log.info("Sign {} with {} Bytes at {}", mimeType.getMimeTypeString(), data.length, signingDate);
    log.debug("Signed Base64 Data:\n{}", Base64.toBase64String(data));
    val inMemDocument = new InMemoryDocument(data);
    inMemDocument.setMimeType(mimeType);

    try (val signingToken =
        new Pkcs12SignatureToken(
            certificate.getCertificateStream().get(), certificate.getP12KeyStoreProtection())) {
      val privateKeyEntry = signingToken.getKeys().get(0);

      val signAlgorithm = getEncryptionAlgorithm(certificate);
      val signParams = getCAdESSignatureParameters(signingDate, privateKeyEntry, signAlgorithm);
      val dataToSign = cades.getDataToSign(inMemDocument, signParams);

      val signatureValue =
          signingToken.sign(dataToSign, signParams.getDigestAlgorithm(), privateKeyEntry);

      log.info("Sign XML with {}", signatureValue);
      val signedDocument =
          (CMSSignedDocument) cades.signDocument(inMemDocument, signParams, signatureValue);
      if (includeRevocationInfo) {
        val ca = BNetzAVLCa.getCA(privateKeyEntry.getCertificate().getCertificate());

        val ocspSource = new OnlineOCSPSource();
        val ocspToken =
            ocspSource.getRevocationToken(
                privateKeyEntry.getCertificate(), new CertificateToken(ca));
        certVerifier.setOcspSource(ocspSource);

        val cmsSignedDataBuilder = new CMSSignedDataBuilder();
        cmsSignedDataBuilder.setOriginalCMSSignedData(signedDocument.getCMSSignedData());
        val cms =
            cmsSignedDataBuilder.extendCMSSignedData(
                Collections.emptyList(), Collections.emptyList(), List.of(ocspToken));
        return cms.getEncoded();
      } else {
        return signedDocument.getCMSSignedData().getEncoded();
      }
    }
  }

  private CAdESSignatureParameters getCAdESSignatureParameters(
      final Date signingDate,
      DSSPrivateKeyEntry privateKeyEntry,
      EncryptionAlgorithm encryptionAlgorithm) {
    val params = new CAdESSignatureParameters();
    params.bLevel().setSigningDate(signingDate);
    params.setEncryptionAlgorithm(encryptionAlgorithm);
    params.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
    params.setSignaturePackaging(SignaturePackaging.ENVELOPING);
    params.setDigestAlgorithm(DigestAlgorithm.SHA256);
    params.setSigningCertificate(privateKeyEntry.getCertificate());
    params.setCertificateChain(privateKeyEntry.getCertificateChain());
    params.setContentHintsType(CMSAttributes.contentHint.getId());
    params.setContentHintsDescription("CMSDocument2sign");
    return params;
  }
}
