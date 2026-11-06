/*
 * Copyright (Change Date see Readme) gematik GmbH
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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Extracts access and nested JWT values from token responses and handles compact JWE payloads
 * encrypted with AES-GCM.
 *
 * <p>The extraction methods locate the corresponding quoted property in the supplied text. The JWE
 * methods use a five-part compact representation containing a protected header, an encrypted-key
 * segment, an initialization vector, ciphertext, and an authentication tag.
 */
public class JweTokenParser {

  private static final Pattern ACCESS_TOKEN_PATTERN =
      Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern NJWT_PATTERN = Pattern.compile("\"njwt\"\\s*:\\s*\"([^\"]+)\"");

  /**
   * Finds the first non-empty {@code access_token} string in a token response body.
   *
   * @param tokenResponseBody response text to search; {@code null} is treated as empty text
   * @return the extracted access token
   * @throws JweTokenParserException if no non-empty {@code access_token} string is found
   */
  public String extractAccessToken(String tokenResponseBody) {
    val matcher = ACCESS_TOKEN_PATTERN.matcher(Objects.requireNonNullElse(tokenResponseBody, ""));
    if (!matcher.find()) {
      throw new JweTokenParserException("access_token not found in token response");
    }
    return matcher.group(1);
  }

  /**
   * Decrypts the payload of a five-part compact JWE using AES-GCM.
   *
   * <p>The protected-header segment is used as authenticated additional data. The supplied key must
   * be a valid AES key, and the JWE authentication tag must verify successfully.
   *
   * @param compactJwe five-part compact JWE string
   * @param tokenKey AES key bytes used to decrypt the payload
   * @return decrypted payload decoded as UTF-8
   * @throws JweTokenParserException if {@code compactJwe} is {@code null} or does not have exactly
   *     five parts
   * @throws GeneralSecurityException if AES-GCM decryption fails, including when the key is invalid
   *     or the authentication tag does not verify
   */
  @SneakyThrows(GeneralSecurityException.class)
  public String decrypt(String compactJwe, byte[] tokenKey) {
    val parts =
        Optional.ofNullable(compactJwe)
            .map(it -> it.split("\\."))
            .filter(p -> p.length == 5)
            .orElseThrow(
                () -> new JweTokenParserException("Invalid JWE format: expected exactly 5 parts"));

    val cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(
        Cipher.DECRYPT_MODE,
        new SecretKeySpec(tokenKey, "AES"),
        new GCMParameterSpec(128, Base64.getUrlDecoder().decode(parts[2])));
    cipher.updateAAD(parts[0].getBytes(StandardCharsets.US_ASCII));

    val ciphertext = Base64.getUrlDecoder().decode(parts[3]);
    val authenticationTag = Base64.getUrlDecoder().decode(parts[4]);
    val combined = new byte[ciphertext.length + authenticationTag.length];
    System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
    System.arraycopy(authenticationTag, 0, combined, ciphertext.length, authenticationTag.length);

    return new String(cipher.doFinal(combined), StandardCharsets.UTF_8);
  }

  /**
   * Encrypts a UTF-8 payload into a five-part compact JWE using direct AES-GCM encryption.
   *
   * <p>The protected header declares {@code alg=dir} and {@code enc=A256GCM}; the key must
   * therefore be 256 bits. The encrypted-key segment is empty. This method uses a zero-filled IV;
   * AES-GCM requires a unique IV for every encryption under a given key, so do not reuse a key
   * across calls.
   *
   * @param payload plaintext to encrypt
   * @param tokenKey 256-bit AES key bytes
   * @return five-part compact JWE string
   * @throws GeneralSecurityException if encryption fails or the key is not valid for AES-GCM
   * @implNote The IV is currently fixed to twelve zero bytes.
   */
  @SneakyThrows(GeneralSecurityException.class)
  public String encrypt(String payload, byte[] tokenKey) {
    val protectedHeader =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                "{\"alg\":\"dir\",\"enc\":\"A256GCM\"}".getBytes(StandardCharsets.UTF_8));
    val iv = new byte[12];
    val cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(
        Cipher.ENCRYPT_MODE, new SecretKeySpec(tokenKey, "AES"), new GCMParameterSpec(128, iv));
    cipher.updateAAD(protectedHeader.getBytes(StandardCharsets.US_ASCII));

    val ciphertextAndTag = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    val ciphertext = Arrays.copyOf(ciphertextAndTag, ciphertextAndTag.length - 16);
    val tag =
        Arrays.copyOfRange(ciphertextAndTag, ciphertextAndTag.length - 16, ciphertextAndTag.length);
    val encoder = Base64.getUrlEncoder().withoutPadding();

    return String.join(
        ".",
        protectedHeader,
        "",
        encoder.encodeToString(iv),
        encoder.encodeToString(ciphertext),
        encoder.encodeToString(tag));
  }

  /**
   * Finds the first non-empty {@code njwt} string in a decrypted payload.
   *
   * @param decryptedPayload plaintext payload to search; {@code null} is treated as empty text
   * @return the extracted nested JWT
   * @throws JweTokenParserException if no non-empty {@code njwt} string is found
   */
  public String extractNestedJwt(String decryptedPayload) {
    val input = Objects.requireNonNullElse(decryptedPayload, "");
    val matcher = NJWT_PATTERN.matcher(input);
    if (!matcher.find()) {
      throw new JweTokenParserException("njwt not found in decrypted payload");
    }
    return matcher.group(1);
  }
}
