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

package de.gematik.bbriccs.rest.idp;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBRequestCustomizer;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.crypto.model.PkiIdentity;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class IdpTokenHeaderProvider implements HttpBRequestCustomizer {

  private final Supplier<IdpTokenResult> authentication;

  private IdpTokenResult idpToken;
  private Instant idpTokenValidUntil =
      Instant.now(); // point in time when the IDP token needs to be updated

  private IdpTokenHeaderProvider(Supplier<IdpTokenResult> authentication) {
    this.authentication = authentication;
  }

  @Override
  public List<HttpHeader> forRequest(HttpBRequest request) {
    this.refreshIdpToken();
    val accessToken = this.idpToken.getAccessToken().getRawString();
    return List.of(JwtHeaderKey.AUTHORIZATION.createHeader(accessToken));
  }

  public void refreshIdpToken() {
    if (needsIdpTokenRefresh()) {
      log.info("Refresh the IDP Token");
      try {
        idpToken = authentication.get();
        idpTokenValidUntil = Instant.now().plus(idpToken.getExpiresIn(), ChronoUnit.SECONDS);
      } catch (NullPointerException npe) {
        // rewrap the NPE to an IdpClientRuntimeException will show tests as compromised instead of
        // broken!
        log.warn("Something went wrong during authentication on IDP");
        throw new IdpClientRuntimeException("Caught NullPointer from IDP-Client", npe);
      }
    } else {
      log.info("IDP Token is still valid, no need to refresh");
    }
  }

  private boolean needsIdpTokenRefresh() {
    val refreshInstant = idpTokenValidUntil.minus(120, ChronoUnit.SECONDS);
    return Instant.now().isAfter(refreshInstant);
  }

  public static IdpTokenHeaderProvider using(Supplier<IdpTokenResult> authentication) {
    return new IdpTokenHeaderProvider(authentication);
  }

  public static JwtHeaderProviderBuilder withDiscoveryDocumentUrl(String url) {
    return new JwtHeaderProviderBuilder(url);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class JwtHeaderProviderBuilder {
    private final String discoveryDocumentUrl;
    private final Set<String> scopes = new LinkedHashSet<>();
    private String redirectUrl;
    private String clientId;

    public JwtHeaderProviderBuilder withRedirectUrl(String redirectUrl) {
      this.redirectUrl = redirectUrl;
      return this;
    }

    public JwtHeaderProviderBuilder withClientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public JwtHeaderProviderBuilder usingScope(String scope) {
      return usingScopes(List.of("openid", scope));
    }

    public JwtHeaderProviderBuilder usingScopes(List<String> scopes) {
      this.scopes.addAll(scopes);
      return this;
    }

    public IdpTokenHeaderProvider authenticateWith(Smartcard smartcard) {
      val autCertificate = smartcard.getAutCertificate();
      val pki =
          PkiIdentity.builder()
              .certificate(autCertificate.getX509Certificate())
              .privateKey(autCertificate.getPrivateKey())
              .build();
      return this.authenticateWith(pki);
    }

    public IdpTokenHeaderProvider authenticateWith(PkiIdentity pki) {
      val idpClient = this.initIdpClient();
      Supplier<IdpTokenResult> authentication = () -> idpClient.login(pki);
      return using(authentication);
    }

    public IdpTokenHeaderProvider authenticateWith(
        X509Certificate authPubCert, UnaryOperator<byte[]> challenge) {
      val idpClient = this.initIdpClient();
      Supplier<IdpTokenResult> authentication = () -> idpClient.login(authPubCert, challenge);
      return using(authentication);
    }

    private IdpClient initIdpClient() {
      val idpClient =
          IdpClient.builder()
              .clientId(this.clientId)
              .redirectUrl(this.redirectUrl)
              .discoveryDocumentUrl(this.discoveryDocumentUrl)
              .scopes(this.scopes)
              .build();
      idpClient.initialize();
      return idpClient;
    }
  }
}
