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

import de.gematik.bbriccs.idp.exception.AuthException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Values of the {@code response_type} authorization request parameter as registered in the IANA
 * "OAuth Parameters" registry. Multi-valued types (OIDC hybrid flow) are space-separated and
 * order-insensitive per RFC 6749 §3.1.1; the canonical order is used here.
 */
@RequiredArgsConstructor
public enum ResponseType implements OAuthParameter {
  /** RFC 6749 §4.1 – authorization code flow */
  CODE("code"),
  /** RFC 6749 §4.2 – implicit flow (deprecated by OAuth 2.0 Security BCP / OAuth 2.1) */
  TOKEN("token"),
  /** OpenID Connect Core 1.0 §3.2 – implicit flow */
  ID_TOKEN("id_token"),
  /** OpenID Connect Core 1.0 §3.2 – implicit flow */
  ID_TOKEN_TOKEN("id_token token"),
  /** OpenID Connect Core 1.0 §3.3 – hybrid flow */
  CODE_ID_TOKEN("code id_token"),
  /** OpenID Connect Core 1.0 §3.3 – hybrid flow */
  CODE_TOKEN("code token"),
  /** OpenID Connect Core 1.0 §3.3 – hybrid flow */
  CODE_ID_TOKEN_TOKEN("code id_token token"),
  /** OAuth 2.0 Multiple Response Type Encoding Practices §4 – no credentials issued */
  NONE("none");

  private final String value;

  @Override
  public String value() {
    return value;
  }

  public static ResponseType from(String value) {
    return optionallyFrom(value)
        .orElseThrow(() -> new AuthException("Unknown response_type: " + value));
  }

  public static Optional<ResponseType> optionallyFrom(String value) {
    return OAuthParameter.fromEnum(value, ResponseType.class);
  }
}
