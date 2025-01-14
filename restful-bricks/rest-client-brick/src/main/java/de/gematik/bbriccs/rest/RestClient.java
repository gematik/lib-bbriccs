/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.bbriccs.rest;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import de.gematik.bbriccs.rest.plugins.HttpBRequestObserver;
import de.gematik.bbriccs.rest.plugins.HttpBResponseObserver;
import de.gematik.bbriccs.rest.plugins.RestObserverManager;
import de.gematik.bbriccs.rest.tls.EmptyTrustManager;
import jakarta.annotation.Nullable;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import kong.unirest.core.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RestClient implements HttpBClient {

  private final UnirestInstance unirest;
  private final String url;
  private final List<HttpHeader> staticHeader;
  private final RestObserverManager restObserver;

  private RestClient(RestClientBuilder builder) {
    this.unirest = builder.unirest;
    this.url = builder.url;
    this.staticHeader = builder.headers;
    this.restObserver = builder.observerBuilder.build();
  }

  public static RestClientBuilder forUrl(String url) {
    return new RestClientBuilder(url);
  }

  @Override
  public void shutDown() {
    this.unirest.close();
  }

  @Override
  public HttpBResponse send(HttpBRequest bRequest) {
    log.trace("Send HTTP Request:\n----------\n{}\n----------", bRequest);
    this.restObserver.serveRequestObservers(bRequest);
    val requestUrl = format("{0}{1}", this.url, bRequest.urlPath());

    HttpRequest<?> httpRequest;
    if (bRequest.method().allowedToHaveBody()) {
      httpRequest =
          this.unirest.request(bRequest.method().name(), requestUrl).body(bRequest.body());
    } else {
      httpRequest = this.unirest.request(bRequest.method().name(), requestUrl);
    }

    this.staticHeader.forEach(h -> httpRequest.header(h.key(), h.value()));
    bRequest.headers().forEach(h -> httpRequest.header(h.key(), h.value()));

    val httpResponse = httpRequest.asBytes();
    val responseHeaders =
        httpResponse.getHeaders().all().stream()
            .map(h -> new HttpHeader(h.getName(), h.getValue()))
            .toList();
    val bResponse =
        new HttpBResponse(
            HttpVersion.HTTP_1_1,
            httpResponse.getStatus(),
            responseHeaders,
            httpResponse.getBody());
    this.restObserver.serveResponseObservers(bResponse);
    return bResponse;
  }

  public static class RestClientBuilder {
    private final String url;
    private final List<HttpHeader> headers;
    private final RestObserverManager.RestObserverBuilder observerBuilder =
        new RestObserverManager.RestObserverBuilder();
    private UnirestInstance unirest;
    @Nullable private Proxy proxy;

    private RestClientBuilder(String url) {
      this.url = url;
      this.headers = new LinkedList<>();
    }

    public RestClientBuilder usingAuthorizationKey(String apiKey) {
      return this.withHeader(AuthHttpHeaderKey.X_AUTHORIZATION.createHeader(apiKey));
    }

    public RestClientBuilder usingApiKey(String apiKey) {
      return this.withHeader(AuthHttpHeaderKey.X_API_KEY.createHeader(apiKey));
    }

    public RestClientBuilder asUserAgent(String userAgent) {
      return this.withHeader(StandardHttpHeaderKey.USER_AGENT.createHeader(userAgent));
    }

    public RestClientBuilder withHeader(String key, String value) {
      return this.withHeader(new HttpHeader(key, value));
    }

    public RestClientBuilder withHeader(HttpHeader header) {
      this.headers.add(header);
      return this;
    }

    public RestClientBuilder withHeaders(List<HttpHeader> httpHeaders) {
      this.headers.addAll(httpHeaders);
      return this;
    }

    public RestClientBuilder registerForRequests(HttpBRequestObserver ro) {
      this.observerBuilder.registerForRequests(ro);
      return this;
    }

    public RestClientBuilder registerForResponses(HttpBResponseObserver ro) {
      this.observerBuilder.registerForResponses(ro);
      return this;
    }

    public RestClientBuilder register(HttpBObserver rro) {
      return this.registerForRequests(rro).registerForResponses(rro);
    }

    public RestClientBuilder usingProxy(String hostName, int port) {
      this.proxy = new Proxy(hostName, port);
      return this;
    }

    public RestClientBuilder usingProxy(
        String hostName, int port, String userName, String password) {
      this.proxy = new Proxy(hostName, port, userName, password);
      return this;
    }

    public RestClient withoutTlsVerification() {
      val trustManager = new EmptyTrustManager();
      return this.withTlsVerification(false, trustManager);
    }

    public RestClient withTlsVerification(X509TrustManager trustManager) {
      return this.withTlsVerification(true, trustManager);
    }

    public RestClient withTlsVerification(SSLContext sslCtx) {
      return withTlsVerification(true, sslCtx);
    }

    public RestClient withTlsVerification(boolean verifySsl, SSLContext sslCtx) {
      this.unirest = Unirest.spawnInstance();

      this.unirest.config().verifySsl(verifySsl).sslContext(sslCtx);
      Optional.ofNullable(this.proxy).ifPresent(p -> this.unirest.config().proxy(p));

      return new RestClient(this);
    }

    @SneakyThrows
    private RestClient withTlsVerification(boolean verifySsl, X509TrustManager trustManager) {
      val sslCtx = SSLContext.getInstance("TLS");
      sslCtx.init(null, new TrustManager[] {trustManager}, new SecureRandom());

      return withTlsVerification(verifySsl, sslCtx);
    }
  }
}
