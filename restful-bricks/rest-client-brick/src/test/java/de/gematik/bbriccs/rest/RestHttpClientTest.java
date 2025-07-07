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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.net.MediaType;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.plugins.BasicHeaderProvider;
import de.gematik.bbriccs.rest.plugins.BasicHttpLogger;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import de.gematik.bbriccs.rest.tls.EmptyTrustManager;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import kong.unirest.core.Interceptor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@WireMockTest
class RestHttpClientTest {

  @RegisterExtension
  static WireMockExtension wm1 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  private static String url;

  private final String apiKey = "test-api-key";

  @BeforeAll
  static void setup() {
    int port = wm1.getPort();
    url = "http://localhost:" + port;
  }

  @SneakyThrows
  private void prepareGetResponse(String testUrl, byte[] resBody) {
    wm1.stubFor(
        get(urlEqualTo(testUrl))
            .withHeader(AuthHttpHeaderKey.X_API_KEY.getKey(), equalTo(apiKey))
            .willReturn(aResponse().withBody(resBody)));

    wm1.stubFor(
        get(urlEqualTo(testUrl))
            .withHeader(AuthHttpHeaderKey.X_API_KEY.getKey(), not(equalTo(apiKey)))
            .willReturn(aResponse().withStatus(403).withBody("Forbidden")));
  }

  @SneakyThrows
  private void preparePostResponse(String testUrl, byte[] resBody) {
    wm1.stubFor(
        post(urlEqualTo(testUrl))
            .withHeader(AuthHttpHeaderKey.X_API_KEY.getKey(), equalTo(apiKey))
            .withRequestBody(equalTo("Body Payload"))
            .willReturn(aResponse().withBody(resBody)));

    wm1.stubFor(
        get(urlEqualTo(testUrl))
            .withHeader(AuthHttpHeaderKey.X_API_KEY.getKey(), not(equalTo(apiKey)))
            .willReturn(aResponse().withStatus(403).withBody("Forbidden")));
  }

  static Stream<Arguments> clientBuilder() {
    return Stream.of(UnirestHttpClient.forUrl(url), BasicHttpClient.forUrl(url)).map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("clientBuilder")
  void shouldNotThrowOnSettingContentLengthWith(HttpClientBuilder<?> clientBuilder) {
    preparePostResponse("/test", "Hello, World!".getBytes());

    val client =
        clientBuilder
            .xApiKey(apiKey)
            .userAgent("Bbriccs-Agent")
            .header(BasicHeaderProvider.forAutoContentLength())
            .register(BasicHttpLogger.toStdout())
            .withoutTlsVerification()
            .init();

    val request = HttpBRequest.post().urlPath("/test").withPayload("Body Payload");
    assertDoesNotThrow(() -> client.send(request));
  }

  @ParameterizedTest
  @MethodSource("clientBuilder")
  void shouldPreserveQueryParameters(HttpClientBuilder<?> clientBuilder) {
    preparePostResponse("/test", "Hello, World!".getBytes());

    val client =
        clientBuilder
            .xApiKey(apiKey)
            .userAgent("Bbriccs-Agent")
            .header(BasicHeaderProvider.forAutoContentLength())
            .withoutTlsVerification()
            .init();

    val request = HttpBRequest.post().urlPath("/test").withPayload("Body Payload");
    assertDoesNotThrow(() -> client.send(request));
  }

  @ParameterizedTest
  @MethodSource("clientBuilder")
  void shouldDetectErrorResponseOnInvalidApiKey(HttpClientBuilder<?> clientBuilder) {
    prepareGetResponse("/test", "Hello, World!".getBytes());

    val client =
        clientBuilder
            .xApiKey("invalid-api-key")
            .xAuthorization("auth-header") // just for coverage
            .userAgent("Bbriccs-Agent")
            .header("X-user", "abc") // just for coverage
            .header(HttpHeader.acceptCharsetUtf8()) // just for coverage
            .header(HttpHeader.accept(MediaType.ANY_TYPE)) // just for coverage
            .headers(List.of()) // just for coverage
            .followRedirects(true) // just for coverage
            .withoutTlsVerification()
            .init();
    val request = HttpBRequest.get().urlPath("/test").withoutPayload();
    val response = assertDoesNotThrow(() -> client.send(request));

    assertEquals(403, response.statusCode());
    assertEquals("Forbidden", response.bodyAsString());

    assertDoesNotThrow(client::shutDown);
  }

  @ParameterizedTest
  @MethodSource("clientBuilder")
  void shouldServeObserverOk(HttpClientBuilder<?> clientBuilder) {
    prepareGetResponse("/test", "Hello, World!".getBytes());

    val httpBop = new ReqResObserver();
    val client =
        clientBuilder
            .xApiKey(apiKey)
            .userAgent("Bbriccs-Agent")
            .register(httpBop)
            //            .register(mock(Interceptor.class))  // TODO
            .withTlsVerification(new EmptyTrustManager()) // just for coverage
            .init();
    val request = HttpBRequest.get().urlPath("/test").withoutPayload();
    val response = assertDoesNotThrow(() -> client.send(request));

    // Note: observermanager serves asynchronously, therefore, we need to wait for the observer to
    // finish
    assertEquals(200, response.statusCode());
    assertEquals("Hello, World!", response.bodyAsString());
    System.out.println(
        format("Spend some time to serve observers for response {0}", response.statusCode()));

    assertEquals(1, httpBop.requests.size());
    assertEquals(1, httpBop.responses.size());

    assertDoesNotThrow(client::shutDown);
  }

  @ParameterizedTest
  @MethodSource("clientBuilder")
  void shouldRethrowSneakyExceptionFromSSLContext(HttpClientBuilder<?> clientBuilder) {
    try (val mockSslContext = mockStatic(SSLContext.class)) {
      mockSslContext
          .when(() -> SSLContext.getInstance("TLS"))
          .thenThrow(NoSuchAlgorithmException.class);
      assertThrows(NoSuchAlgorithmException.class, clientBuilder::withoutTlsVerification);
    }
  }

  @ParameterizedTest
  @MethodSource("clientBuilder")
  void shouldSetProxyAndSsl(HttpClientBuilder<?> clientBuilder) {
    // only required for coverage...
    clientBuilder.proxy("https://localhost", 3128);
    val sslContextMock = mock(SSLContext.class);
    assertDoesNotThrow(() -> clientBuilder.withTlsVerification(sslContextMock));
  }

  @Test
  void shouldSetCustomInterceptorsForUnirest() {
    // only required for coverage...
    val builder = UnirestHttpClient.forUrl(url);
    val interceptor = mock(Interceptor.class);
    assertDoesNotThrow(() -> builder.register(interceptor));
  }

  private static class ReqResObserver implements HttpBObserver {

    private final List<HttpBRequest> requests = new LinkedList<>();
    private final List<HttpBResponse> responses = new LinkedList<>();

    @Override
    public void onRequest(HttpBRequest request) {
      this.requests.add(request);
    }

    @Override
    public void onResponse(HttpBResponse response) {
      this.responses.add(response);
    }
  }
}
