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

package de.gematik.bbriccs.rest;

import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import de.gematik.bbriccs.rest.plugins.HttpBRequestObserver;
import de.gematik.bbriccs.rest.plugins.HttpBResponseObserver;
import de.gematik.bbriccs.rest.plugins.RequestHeaderProvider;
import de.gematik.bbriccs.rest.plugins.RestObserverManager;
import de.gematik.bbriccs.rest.tls.EmptyTrustManager;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.SneakyThrows;
import lombok.val;

public abstract class HttpClientBuilder<B extends HttpClientBuilder<B>> {

  protected final String url;
  protected final List<HttpHeader> headers;
  protected final List<RequestHeaderProvider> dynamicHeaders;
  protected final RestObserverManager.RestObserverBuilder observerBuilder =
      new RestObserverManager.RestObserverBuilder();
  protected boolean followRedirects = false;

  protected HttpClientBuilder(String url) {
    this.url = url;
    this.headers = new LinkedList<>();
    this.dynamicHeaders = new LinkedList<>();
  }

  @SuppressWarnings("unchecked")
  private B self() {
    return (B) this;
  }

  public B followRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return self();
  }

  public B xAuthorization(String apiKey) {
    return this.header(AuthHttpHeaderKey.X_AUTHORIZATION.createHeader(apiKey));
  }

  public B xApiKey(String apiKey) {
    return this.header(AuthHttpHeaderKey.X_API_KEY.createHeader(apiKey));
  }

  public B userAgent(String userAgent) {
    return this.header(StandardHttpHeaderKey.USER_AGENT.createHeader(userAgent));
  }

  public B header(String key, String value) {
    return this.header(new HttpHeader(key, value));
  }

  public B header(HttpHeader... header) {
    this.headers.addAll(List.of(header));
    return self();
  }

  public B headers(List<HttpHeader> httpHeaders) {
    this.headers.addAll(httpHeaders);
    return self();
  }

  public B header(RequestHeaderProvider... dynamicHeader) {
    this.dynamicHeaders.addAll(List.of(dynamicHeader));
    return self();
  }

  public B registerForRequests(HttpBRequestObserver ro) {
    this.observerBuilder.registerForRequests(ro);
    return self();
  }

  public B registerForResponses(HttpBResponseObserver ro) {
    this.observerBuilder.registerForResponses(ro);
    return self();
  }

  public B register(HttpBObserver rro) {
    return this.registerForRequests(rro).registerForResponses(rro);
  }

  public HttpBClient withoutTlsVerification() {
    val trustManager = new EmptyTrustManager();
    return this.withTlsVerification(false, trustManager);
  }

  public HttpBClient withTlsVerification(X509TrustManager trustManager) {
    return this.withTlsVerification(true, trustManager);
  }

  public HttpBClient withTlsVerification(SSLContext sslCtx) {
    return withTlsVerification(true, sslCtx);
  }

  @SneakyThrows
  protected HttpBClient withTlsVerification(boolean verifySsl, X509TrustManager trustManager) {
    val sslCtx = SSLContext.getInstance("TLS");
    sslCtx.init(null, new TrustManager[] {trustManager}, new SecureRandom());

    return withTlsVerification(verifySsl, sslCtx);
  }

  protected abstract HttpBClient withTlsVerification(boolean verifySsl, SSLContext sslCtx);

  public abstract B proxy(String hostName, int port);
}
