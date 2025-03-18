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
 */

package de.gematik.bbriccs.rest.idp;

import de.gematik.bbriccs.rest.fd.FdRequest;
import de.gematik.bbriccs.rest.fd.plugins.RequestHeaderProvider;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.crypto.model.PkiIdentity;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class IdpTokenHeaderProvider implements RequestHeaderProvider {

  private final Supplier<IdpTokenResult> authentication;

  private IdpTokenResult idpToken;
  private Instant idpTokenUpdated; // point in time when the IDP token was updated

  private IdpTokenHeaderProvider(Supplier<IdpTokenResult> authentication) {
    this.authentication = authentication;
  }

  @Override
  public HttpHeader forRequest(FdRequest<? extends Resource, ? extends Resource> request) {
    this.refreshIdpToken();
    val accessKey = this.idpToken.getAccessToken().getRawString();
    return JwtHeaderKey.AUTHORIZATION.createHeader(accessKey);
  }

  private void refreshIdpToken() {
    if (idpTokenExpired()) {
      log.info("Refresh the IDP Token");
      try {
        this.idpToken = this.authentication.get();
        this.idpTokenUpdated = Instant.now();
      } catch (NullPointerException npe) {
        // rewrap the NPE to an IdpClientRuntimeException
        log.warn("Something went wrong during authentication on IDP");
        throw new IdpClientRuntimeException("Caught NullPointer from IDP-Client", npe);
      }
    } else {
      log.info("IDP Token is still valid, no need to refresh");
    }
  }

  private boolean idpTokenExpired() {
    boolean ret;
    if (this.idpToken == null) {
      ret = true; // actually not expired but hasn't been fetched yet
    } else {
      val now = Instant.now();
      val diff = Duration.between(this.idpTokenUpdated, now).getSeconds();
      ret = diff >= this.idpToken.getExpiresIn();
    }
    return ret;
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
      return new IdpTokenHeaderProvider(authentication);
    }

    public IdpTokenHeaderProvider authenticateWith(
        X509Certificate authPubCert, UnaryOperator<byte[]> challenge) {
      val idpClient = this.initIdpClient();
      Supplier<IdpTokenResult> authentication = () -> idpClient.login(authPubCert, challenge);
      return new IdpTokenHeaderProvider(authentication);
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
