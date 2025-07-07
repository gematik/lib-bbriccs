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

package de.gematik.bbriccs.idp.factory;

import static org.jose4j.jws.AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;
import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
import static org.jose4j.jws.EcdsaUsingShaAlgorithm.convertDerToConcatenated;

import de.gematik.bbriccs.idp.data.Nonce;
import de.gematik.bbriccs.idp.exception.SignatureBException;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.idp.field.ClaimName;
import de.gematik.idp.token.JsonWebToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

@Deprecated // TODO: rewrite to be more flexible
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtFactory {

  public static String signJwt(
      Smartcard smartcard, UnaryOperator<byte[]> contentSigner, Nonce nonce) {

    val autCert = smartcard.getAutCertificate().getX509Certificate();
    val claims = new JwtClaims();
    claims.setClaim(ClaimName.NONCE.getJoseName(), nonce.value());
    claims.setClaim(ClaimName.ISSUED_AT.getJoseName(), ZonedDateTime.now().toEpochSecond());
    claims.setClaim(
        ClaimName.EXPIRES_AT.getJoseName(), ZonedDateTime.now().plusMinutes(20).toEpochSecond());
    return createSignedJwt(
        claims, autCert, contentSigner, determineAlgorithm(autCert.getPublicKey()));
  }

  private static String createSignedJwt(
      JwtClaims claims,
      X509Certificate certificate,
      UnaryOperator<byte[]> contentSigner,
      String algorithm) {

    val jsonWebSignature = new JsonWebSignature();
    jsonWebSignature.setHeader("typ", "JWT");
    jsonWebSignature.setCertificateChainHeaderValue(certificate);
    jsonWebSignature.setPayload(claims.toJson());
    jsonWebSignature.setAlgorithmHeaderValue(algorithm);

    val signedJwt =
        jsonWebSignature.getHeaders().getEncodedHeader()
            + "."
            + jsonWebSignature.getEncodedPayload()
            + "."
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    getSignatureBytes(
                        contentSigner,
                        jsonWebSignature,
                        sigData -> {
                          if (certificate.getPublicKey() instanceof RSAPublicKey) {
                            return sigData;
                          } else {
                            try {
                              return convertDerToConcatenated(sigData, 64);
                            } catch (final IOException e) {
                              throw new SignatureBException("Error while signing JWT", e);
                            }
                          }
                        }));

    return new JsonWebToken(signedJwt).getRawString();
  }

  private static byte[] getSignatureBytes(
      final UnaryOperator<byte[]> contentSigner,
      final JsonWebSignature jsonWebSignature,
      final UnaryOperator<byte[]> signatureStripper) {
    return signatureStripper.apply(
        contentSigner.apply(
            (jsonWebSignature.getHeaders().getEncodedHeader()
                    + "."
                    + jsonWebSignature.getEncodedPayload())
                .getBytes(StandardCharsets.UTF_8)));
  }

  public static String determineAlgorithm(PublicKey signerKey) {
    if (signerKey instanceof ECPublicKey) {
      // based on A_24590 it has to be ES256 even if brainpoolP256r1 is used
      return ECDSA_USING_P256_CURVE_AND_SHA256;
    } else {
      return RSA_PSS_USING_SHA256;
    }
  }
}
