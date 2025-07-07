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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.RequestHeaderProvider;
import de.gematik.bbriccs.rest.plugins.RestObserverManager;
import jakarta.annotation.Nullable;
import java.net.http.HttpClient.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import kong.unirest.core.HttpRequest;
import kong.unirest.core.Interceptor;
import kong.unirest.core.Proxy;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class UnirestHttpClient implements HttpBClient {

  private final UnirestInstance unirest;
  private final String url;
  private final List<HttpHeader> staticHeaders;
  private final List<RequestHeaderProvider> dynamicHeaders;
  private final RestObserverManager restObserver;

  private UnirestHttpClient(UniRestHttpClientBuilder builder) {
    this.unirest = builder.unirest;
    this.url = builder.url;
    this.staticHeaders = builder.headers;
    this.dynamicHeaders = builder.dynamicHeaders;
    this.restObserver = builder.observerBuilder.build();
  }

  public static UniRestHttpClientBuilder forUrl(String url) {
    return new UniRestHttpClientBuilder(url);
  }

  @Override
  public void shutDown() {
    this.unirest.close();
  }

  @Override
  public HttpBResponse send(HttpBRequest bRequest) {
    log.trace("Send HTTP Request:\n----------\n{}\n----------", bRequest);
    bRequest.addIfAbsentHeader(this.staticHeaders);
    this.dynamicHeaders.stream()
        .map(p -> p.forRequest(bRequest))
        .filter(Objects::nonNull)
        .forEach(bRequest::addIfAbsentHeader);

    this.restObserver.serveRequestObservers(bRequest);
    val requestUrl = format("{0}{1}", this.url, bRequest.urlPath());

    HttpRequest<?> httpRequest;
    if (bRequest.method().allowedToHaveBody()) {
      httpRequest =
          this.unirest.request(bRequest.method().name(), requestUrl).body(bRequest.body());
    } else {
      httpRequest = this.unirest.request(bRequest.method().name(), requestUrl);
    }

    // unirest does not allow you to set content-length by hand
    bRequest.removeHeader(StandardHttpHeaderKey.CONTENT_LENGTH);
    bRequest.headers().forEach(h -> httpRequest.header(h.key(), h.value()));

    val httpResponse = httpRequest.asBytes();
    val responseHeaders =
        httpResponse.getHeaders().all().stream()
            .map(h -> new HttpHeader(h.getName(), h.getValue()))
            .toList();
    val bResponse =
        HttpBResponse.status(httpResponse.getStatus())
            .version(HttpVersion.HTTP_1_1)
            .headers(responseHeaders)
            .withPayload(httpResponse.getBody());

    this.restObserver.serveResponseObservers(bResponse);
    return bResponse;
  }

  public static class UniRestHttpClientBuilder extends HttpClientBuilder<UniRestHttpClientBuilder> {

    private final List<Interceptor> unirestInterceptors = new ArrayList<>();
    private UnirestInstance unirest;
    @Nullable private Proxy proxy;

    private UniRestHttpClientBuilder(String url) {
      super(url);
    }

    public UniRestHttpClientBuilder register(Interceptor interceptor) {
      this.unirestInterceptors.add(interceptor);
      return this;
    }

    @Override
    public UniRestHttpClientBuilder proxy(String hostName, int port) {
      this.proxy = new Proxy(hostName, port);
      return this;
    }

    @Override
    protected HttpBClient withTlsVerification(boolean verifySsl, SSLContext sslCtx) {
      this.unirest = Unirest.spawnInstance();
      val unirestConfig = this.unirest.config();

      unirestConfig
          .version(Version.HTTP_1_1)
          .verifySsl(verifySsl)
          .sslContext(sslCtx)
          .followRedirects(followRedirects);
      Optional.ofNullable(this.proxy).ifPresent(unirestConfig::proxy);

      this.unirestInterceptors.forEach(unirestConfig::interceptor);

      return new UnirestHttpClient(this);
    }
  }
}
