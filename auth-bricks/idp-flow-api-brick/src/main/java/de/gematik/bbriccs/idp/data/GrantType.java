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

package de.gematik.bbriccs.idp.data;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Values of the {@code grant_type} token request parameter as registered in the IANA "OAuth
 * Parameters" registry.
 */
@RequiredArgsConstructor
public enum GrantType implements OAuthParameter {
  /** RFC 6749 §4.1 */
  AUTHORIZATION_CODE("authorization_code"),
  /** RFC 6749 §4.3 (deprecated by OAuth 2.0 Security BCP / OAuth 2.1) */
  PASSWORD("password"),
  /** RFC 6749 §4.4 */
  CLIENT_CREDENTIALS("client_credentials"),
  /** RFC 6749 §6 */
  REFRESH_TOKEN("refresh_token"),
  /** RFC 7523 §2.1 */
  JWT_BEARER("urn:ietf:params:oauth:grant-type:jwt-bearer"),
  /** RFC 7522 §2.1 */
  SAML2_BEARER("urn:ietf:params:oauth:grant-type:saml2-bearer"),
  /** RFC 8628 §3.4 */
  DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code"),
  /** RFC 8693 §2.1 */
  TOKEN_EXCHANGE("urn:ietf:params:oauth:grant-type:token-exchange"),
  /** OpenID Connect CIBA Core 1.0 §10.1 */
  CIBA("urn:openid:params:grant-type:ciba");

  private final String value;

  @Override
  public String value() {
    return value;
  }

  public static Optional<GrantType> from(String value) {
    return OAuthParameter.fromEnum(value, GrantType.class);
  }
}
