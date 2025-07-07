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

import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.RequestHeaderProvider;
import de.gematik.bbriccs.rest.plugins.RestObserverManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import lombok.val;

public class BasicHttpClient implements HttpBClient {

  private final String url;
  private final HttpClient httpClient;

  private final List<HttpHeader> headers;
  private final List<RequestHeaderProvider> dynamicHeaders;
  private final RestObserverManager restObserver;

  private BasicHttpClient(BasicHttpClientBuilder builder, HttpClient httpClient) {
    this.url = builder.url;
    this.headers = builder.headers;
    this.dynamicHeaders = builder.dynamicHeaders;
    this.restObserver = builder.observerBuilder.build();
    this.httpClient = httpClient;
  }

  @SneakyThrows
  @Override
  public HttpBResponse send(HttpBRequest bRequest) {
    bRequest.addIfAbsentHeader(headers);
    this.dynamicHeaders.stream()
        .map(p -> p.forRequest(bRequest))
        .filter(Objects::nonNull)
        .forEach(bRequest::addIfAbsentHeader);

    this.restObserver.serveRequestObservers(bRequest);

    val rb =
        HttpRequest.newBuilder(URI.create(url + bRequest.urlPath()))
            .version(bRequest.version().asVersion());

    // java.net.http does not allow you to set content-length by hand
    bRequest.removeHeader(StandardHttpHeaderKey.CONTENT_LENGTH);
    bRequest.headers().forEach(header -> rb.header(header.key(), header.value()));

    val body = bRequest.body();

    // automatically calculate content-length for the request!
    val bodyPublisher =
        bRequest.isEmptyBody()
            ? BodyPublishers.ofByteArray(body)
            : BodyPublishers.fromPublisher(BodyPublishers.ofByteArray(body), body.length);
    rb.method(bRequest.method().name(), bodyPublisher);
    val request = rb.build();

    val response = httpClient.send(request, BodyHandlers.ofByteArray());

    val responseCode = response.statusCode();
    val responseHeaders =
        response.headers().map().entrySet().stream()
            .map(entry -> new HttpHeader(entry.getKey(), entry.getValue().get(0)))
            .toList();

    val version = HttpVersion.HTTP_1_1;
    val bResponse =
        HttpBResponse.status(responseCode)
            .version(version)
            .headers(responseHeaders)
            .withPayload(response.body());

    this.restObserver.serveResponseObservers(bResponse);
    return bResponse;
  }

  public static BasicHttpClientBuilder forUrl(String url) {
    return new BasicHttpClientBuilder(url);
  }

  public static class BasicHttpClientBuilder extends HttpClientBuilder<BasicHttpClientBuilder> {
    @Nullable private ProxySelector proxySelector;

    private BasicHttpClientBuilder(String url) {
      super(url);
    }

    @Override
    public BasicHttpClientBuilder proxy(String hostName, int port) {
      this.proxySelector = ProxySelector.of(InetSocketAddress.createUnresolved(hostName, port));
      return this;
    }

    @Override
    protected HttpBClient withTlsVerification(boolean verifySsl, SSLContext sslCtx) {
      val redirect = followRedirects ? Redirect.ALWAYS : Redirect.NEVER;
      val httpClientBuilder =
          HttpClient.newBuilder()
              .version(HttpClient.Version.HTTP_1_1)
              .followRedirects(redirect)
              .sslContext(sslCtx)
              .connectTimeout(Duration.of(5, ChronoUnit.MINUTES));

      Optional.ofNullable(this.proxySelector).ifPresent(httpClientBuilder::proxy);

      return new BasicHttpClient(this, httpClientBuilder.build());
    }
  }
}
