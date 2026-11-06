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

package de.gematik.bbriccs.gid.config;

import com.google.common.base.Strings;
import de.gematik.bbriccs.gid.exception.GidClientConfigurationException;
import de.gematik.bbriccs.idp.data.ClientId;
import de.gematik.bbriccs.idp.data.CodeChallenge;
import de.gematik.bbriccs.idp.data.RedirectUri;
import de.gematik.bbriccs.idp.data.Scope;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@Getter
public class GidAuthenticationConfig {

  private final String fedAuthEndpoint;
  private final String tokenEndpoint;
  private final String sekIdpIss;
  private final ClientId clientId;
  private final RedirectUri redirectUri;
  private final List<Scope> scope;
  private final String kvnr;

  private final CodeChallenge codeChallenge;
  private final String codeVerifier;
  private final String keyVerifier;
  private final byte[] tokenKey;

  private GidAuthenticationConfig(Builder builder) {
    this.fedAuthEndpoint = builder.fedAuthEndpoint;
    this.tokenEndpoint = builder.tokenEndpoint;
    this.sekIdpIss = builder.sekIdpIss;
    this.clientId = builder.clientId;
    this.redirectUri = builder.redirectUri;
    this.scope = List.copyOf(builder.scope);
    this.kvnr = builder.kvnr;
    this.codeChallenge = builder.codeChallenge;
    this.codeVerifier = builder.codeVerifier;
    // Note: codeVerifier and keyVerifier are mostly the same,
    this.keyVerifier = Optional.ofNullable(builder.keyVerifier).orElse(this.codeVerifier);
    this.tokenKey = builder.tokenKey;
  }

  public String getScopeValue() {
    return scope.stream().map(Scope::value).collect(Collectors.joining(" "));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String fedAuthEndpoint;
    private String tokenEndpoint;
    private String sekIdpIss;
    private ClientId clientId;
    private RedirectUri redirectUri;
    private final List<Scope> scope;
    private String kvnr;
    private CodeChallenge codeChallenge;
    private String codeVerifier;
    private String keyVerifier;
    private byte[] tokenKey;

    Builder() {
      this.scope = new ArrayList<>();
    }

    public Builder fedAuthEndpoint(String fedAuthEndpoint) {
      this.fedAuthEndpoint = fedAuthEndpoint;
      return this;
    }

    public Builder tokenEndpoint(String tokenEndpoint) {
      this.tokenEndpoint = tokenEndpoint;
      return this;
    }

    public Builder sekIdpIss(String sekIdpIss) {
      this.sekIdpIss = sekIdpIss;
      return this;
    }

    public Builder clientId(ClientId clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder clientId(String clientId) {
      return this.clientId(ClientId.from(clientId));
    }

    public Builder redirectUri(RedirectUri redirectUri) {
      this.redirectUri = redirectUri;
      return this;
    }

    public Builder redirectUri(String redirectUri) {
      return this.redirectUri(RedirectUri.from(redirectUri));
    }

    public Builder scope(List<Scope> scopes) {
      this.scope.addAll(scopes);
      return this;
    }

    public Builder scope(Scope... scopes) {
      return this.scope(List.of(scopes));
    }

    public Builder kvnr(String kvnr) {
      this.kvnr = kvnr;
      return this;
    }

    public Builder codeChallenge(CodeChallenge codeChallenge) {
      this.codeChallenge = codeChallenge;
      return this;
    }

    public Builder codeChallenge(String codeChallenge) {
      return this.codeChallenge(CodeChallenge.from(codeChallenge));
    }

    public Builder codeVerifier(String codeVerifier) {
      this.codeVerifier = codeVerifier;
      return this;
    }

    public Builder keyVerifier(String keyVerifier) {
      this.keyVerifier = keyVerifier;
      return this;
    }

    public Builder tokenKey(byte[] tokenKey) {
      this.tokenKey = tokenKey;
      return this;
    }

    /**
     * Sets the token key from a Base64 URL-safe encoded string (RFC 4648 §5, alphabet {@code
     * A-Za-z0-9-_}, padding optional).
     *
     * <p>The given string MUST be valid Base64 URL encoded. Decoding happens immediately: if the
     * string contains characters outside the Base64 URL alphabet (e.g. {@code +} or {@code /} from
     * standard Base64), a {@link GidClientConfigurationException} is thrown right away, not
     * deferred to {@link #build()}. A {@code null} value is treated as an empty key and is rejected
     * by {@link #build()} as a missing value.
     *
     * @param tokenKey the Base64 URL encoded token key
     * @return the builder instance
     * @throws GidClientConfigurationException if {@code tokenKey} is not a valid Base64 URL encoded
     *     string
     */
    public Builder tokenKey(String tokenKey) {
      try {
        return this.tokenKey(Base64.getUrlDecoder().decode(Strings.nullToEmpty(tokenKey)));
      } catch (Exception e) {
        throw new GidClientConfigurationException("Invalid Base64 URL encoded tokenKey", e);
      }
    }

    /**
     * @throws GidClientConfigurationException if any required value is missing; the message lists
     *     all missing values at once
     */
    public GidAuthenticationConfig build() {
      validate();
      return new GidAuthenticationConfig(this);
    }

    private void validate() {
      val missing = new ArrayList<String>();
      requireText(missing, "fedAuthEndpoint", fedAuthEndpoint);
      requireText(missing, "tokenEndpoint", tokenEndpoint);
      requireText(missing, "sekIdpIss", sekIdpIss);
      requireValue(missing, "clientId", clientId);
      requireValue(missing, "redirectUri", redirectUri);
      if (scope.isEmpty()) {
        missing.add("scope");
      }
      requireText(missing, "kvnr", kvnr);
      requireValue(missing, "codeChallenge", codeChallenge);
      requireText(missing, "codeVerifier", codeVerifier);
      // keyVerifier is optional, defaults to codeVerifier
      if (tokenKey == null || tokenKey.length == 0) {
        missing.add("tokenKey");
      }

      if (!missing.isEmpty()) {
        throw new GidClientConfigurationException(
            "Missing required GidAuthenticationConfig values: " + String.join(", ", missing));
      }
    }

    private static void requireValue(List<String> missing, String name, Object value) {
      if (value == null) {
        missing.add(name);
      }
    }

    private static void requireText(List<String> missing, String name, String value) {
      if (StringUtils.isBlank(value)) {
        missing.add(name);
      }
    }
  }
}
