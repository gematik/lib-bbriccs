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
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.idp.crypto.EcSignerUtility;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.idp.field.ClaimName;
import de.gematik.idp.tests.PkiKeyResolver;
import de.gematik.idp.token.JsonWebToken;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PkiKeyResolver.class)
class JsonWebTokenBuilderTest {

  public static final String VALUE_FOR_CLAIM_TYPE_EPA = "ePA-Authentisierung über PKI";

  @Test
  void testGenerateAndSignTokenWithPkiIdentity(
      @PkiKeyResolver.Filename("ecc") final PkiIdentity tokenSignerIdentity) {
    final JsonWebToken jsonWebToken =
        JsonWebTokenBuilder.withEpaAlgorithmIdentifier()
            .signedBy(tokenSignerIdentity)
            .withClaim(ClaimName.DISPLAY_NAME, "blafusel")
            .issuedAt(ZonedDateTime.now())
            .generateAndSign();
    assertNotNull(jsonWebToken);
  }

  @Test
  void testGenerateAndSignTokenWithContentSigner(
      @PkiKeyResolver.Filename("ecc") final PkiIdentity tokenSignerIdentity) {
    final UnaryOperator<byte[]> contentSigner =
        tbsData -> EcSignerUtility.createEcSignature(tbsData, tokenSignerIdentity.getPrivateKey());
    final JsonWebToken jsonWebToken =
        JsonWebTokenBuilder.withEpaAlgorithmIdentifier()
            .withClaim(ClaimName.DISPLAY_NAME, "blafusel")
            .issuedAt(ZonedDateTime.now())
            .generateAndSign(tokenSignerIdentity.getCertificate(), contentSigner);
    assertNotNull(jsonWebToken);
  }

  @Test
  void testGenerateErezeptTokenForEpaAs(
      @PkiKeyResolver.Filename("ecc") final PkiIdentity tokenSignerIdentity) {
    final JsonWebToken jsonWebToken =
        JsonWebTokenBuilder.withEpaAlgorithmIdentifier()
            .signedBy(tokenSignerIdentity)
            .withClaim(ClaimName.SUBJECT, "https://erezept.fachdienst.de")
            .withClaim("type", VALUE_FOR_CLAIM_TYPE_EPA)
            .withClaim("challenge", "myEpaChallenge")
            .issuedAt(ZonedDateTime.now())
            .generateAndSign();
    assertTrue(
        jsonWebToken.extractHeaderClaims().keySet().containsAll(Set.of("typ", "alg", "x5c")));
    assertTrue(
        jsonWebToken
            .extractBodyClaims()
            .keySet()
            .containsAll(Set.of("type", "challenge", "sub", "iat")));
    assertTrue(jsonWebToken.getBodyClaim(ClaimName.EXPIRES_AT).isEmpty());
  }

  @Test
  void testGenerateTokenWithBrainpoolSignature(
      @PkiKeyResolver.Filename("ecc") final PkiIdentity tokenSignerIdentity) {
    final JsonWebToken jsonWebToken =
        JsonWebTokenBuilder.withBrainpoolAlgorithmIdentifier()
            .signedBy(tokenSignerIdentity)
            .withClaim(ClaimName.SUBJECT, "https://idpdienst.de")
            .issuedAt(ZonedDateTime.now())
            .generateAndSign();
    assertTrue(
        jsonWebToken.extractHeaderClaims().keySet().containsAll(Set.of("typ", "alg", "x5c")));
    final Optional<Object> alg = jsonWebToken.getHeaderClaim(ClaimName.ALGORITHM);
    assertTrue(alg.isPresent());
    assertEquals(ALG_BP256R1, alg.get());
  }

  @Test
  void testGenerateTokenWithExpAndWithoutIat(
      @PkiKeyResolver.Filename("ecc") final PkiIdentity tokenSignerIdentity) {
    final JsonWebToken jsonWebToken =
        JsonWebTokenBuilder.withEpaAlgorithmIdentifier()
            .signedBy(tokenSignerIdentity)
            .withClaim(ClaimName.SUBJECT, "https://erezept.fachdienst.de")
            .expiredAt(ZonedDateTime.now().plusDays(1))
            .generateAndSign();
    assertTrue(jsonWebToken.getBodyClaim(ClaimName.EXPIRES_AT).isPresent());
    assertTrue(jsonWebToken.getBodyClaim(ClaimName.ISSUED_AT).isEmpty());
  }

  @Test
  void testGeneratingTokenWithoutSignerResultsInException() {

    final JsonWebTokenBuilder jsonWebTokenBuilder =
        JsonWebTokenBuilder.withEpaAlgorithmIdentifier()
            .withClaim("myclaim", "blabla")
            .expiredAt(ZonedDateTime.now().plusDays(1));
    assertThrows(NullPointerException.class, jsonWebTokenBuilder::generateAndSign);
  }

  @Test
  void testGenerateAndSignInvalidDataResultsInJWTException(
      @PkiKeyResolver.Filename("ecc") final PkiIdentity tokenSignerIdentity) {
    final UnaryOperator<byte[]> contentSigner =
        tbsData -> "invalidStuff".getBytes(StandardCharsets.UTF_8);
    final JsonWebTokenBuilder jsonWebTokenBuilder =
        JsonWebTokenBuilder.withEpaAlgorithmIdentifier()
            .withClaim(ClaimName.DISPLAY_NAME, "blafusel")
            .issuedAt(ZonedDateTime.now());
    final X509Certificate certificate = tokenSignerIdentity.getCertificate();
    assertThrows(
        JsonWebTokenBuilderException.class,
        () -> jsonWebTokenBuilder.generateAndSign(certificate, contentSigner));
  }
}
