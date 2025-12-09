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

package de.gematik.bbriccs.jwt;

import static de.gematik.bbriccs.jwt.Constants.ALG_BP256R1;
import static de.gematik.bbriccs.jwt.Constants.ALG_ES256;
import static org.jose4j.jws.EcdsaUsingShaAlgorithm.convertDerToConcatenated;

import de.gematik.bbriccs.crypto.BC;
import de.gematik.idp.crypto.EcSignerUtility;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.idp.field.ClaimName;
import de.gematik.idp.token.JsonWebToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

public class JsonWebTokenBuilder {

  private final Map<String, Object> bodyClaims;
  private ZonedDateTime issuedAt;
  private ZonedDateTime expiresAt;
  private PkiIdentity tokenSignerIdentity;
  private String signatureAlgorithm;

  static {
    BC.init();
  }

  private JsonWebTokenBuilder() {
    this.bodyClaims = new HashMap<>();
  }

  public static JsonWebTokenBuilder withEpaAlgorithmIdentifier() {
    final JsonWebTokenBuilder builder = new JsonWebTokenBuilder();
    builder.signatureAlgorithm = ALG_ES256;
    return builder;
  }

  public static JsonWebTokenBuilder withBrainpoolAlgorithmIdentifier() {
    final JsonWebTokenBuilder builder = new JsonWebTokenBuilder();
    builder.signatureAlgorithm = ALG_BP256R1;
    return builder;
  }

  public JsonWebTokenBuilder withClaim(final String key, final Object value) {
    this.bodyClaims.put(key, value);
    return this;
  }

  public JsonWebTokenBuilder withClaim(final ClaimName claimName, final Object value) {
    this.bodyClaims.put(claimName.getJoseName(), value);
    return this;
  }

  // (4) Set issued at timestamp
  public JsonWebTokenBuilder issuedAt(final ZonedDateTime issuedAt) {
    this.issuedAt = issuedAt;
    return this;
  }

  public JsonWebTokenBuilder expiredAt(final ZonedDateTime expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  // (5) Set token signer and build the token
  public JsonWebTokenBuilder signedBy(final PkiIdentity tokenSignerIdentity) {
    this.tokenSignerIdentity = tokenSignerIdentity;
    return this;
  }

  private static UnaryOperator<byte[]> getEccSigner(final PkiIdentity pkiIdentity) {
    return tbsData -> EcSignerUtility.createEcSignature(tbsData, pkiIdentity.getPrivateKey());
  }

  public JsonWebToken generateAndSign() {
    return generateAndSign(tokenSignerIdentity.getCertificate(), getEccSigner(tokenSignerIdentity));
  }

  public JsonWebToken generateAndSign(
      final X509Certificate certificate, final UnaryOperator<byte[]> contentSigner) {
    final JsonWebSignature jsonWebSignature = new JsonWebSignature();
    final Map<String, Object> claimsMap = new HashMap<>(bodyClaims);
    if (issuedAt != null) {
      claimsMap.put(ClaimName.ISSUED_AT.getJoseName(), issuedAt.toEpochSecond());
    }
    if (expiresAt != null) {
      claimsMap.put(ClaimName.EXPIRES_AT.getJoseName(), expiresAt.toEpochSecond());
    }
    final JwtClaims claims = new JwtClaims();
    claimsMap.forEach(claims::setClaim);
    jsonWebSignature.setPayload(claims.toJson());
    jsonWebSignature.setHeader("typ", "JWT");
    jsonWebSignature.setCertificateChainHeaderValue(certificate);
    jsonWebSignature.setAlgorithmHeaderValue(signatureAlgorithm);
    final String signedJwt =
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
                          try {
                            return convertDerToConcatenated(sigData, 64);
                          } catch (final IOException e) {
                            throw new JsonWebTokenBuilderException(e.getMessage());
                          }
                        }));
    return new JsonWebToken(signedJwt);
  }

  private byte[] getSignatureBytes(
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
}
