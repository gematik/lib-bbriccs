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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import de.gematik.bbriccs.rest.tls.EmptyTrustManager;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest
class RestClientTest {

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

  @Test
  void shouldMakeSimpleCall() {
    preparePostResponse("/test", "Hello, World!".getBytes());

    val client =
        RestClient.forUrl(url)
            .usingApiKey(apiKey)
            .asUserAgent("Bbriccs-Agent")
            .withoutTlsVerification()
            .init();
    val request = new HttpBRequest(HttpRequestMethod.POST, "/test", "Body Payload");
    val response = assertDoesNotThrow(() -> client.send(request));

    assertEquals(200, response.statusCode());
    assertEquals("Hello, World!", response.bodyAsString());

    assertDoesNotThrow(client::shutDown);
  }

  @Test
  void shouldDetectErrorResponseOnInvalidApiKey() {
    prepareGetResponse("/test", "Hello, World!".getBytes());

    val client =
        RestClient.forUrl(url)
            .usingApiKey("invalid-api-key")
            .usingAuthorizationKey("auth-header") // just for coverage
            .asUserAgent("Bbriccs-Agent")
            .withHeader("X-user", "abc") // just for coverage
            .withHeaders(List.of()) // just for coverage
            .withoutTlsVerification()
            .init();
    val request = new HttpBRequest(HttpRequestMethod.GET, "/test");
    val response = assertDoesNotThrow(() -> client.send(request));

    assertEquals(403, response.statusCode());
    assertEquals("Forbidden", response.bodyAsString());

    assertDoesNotThrow(client::shutDown);
  }

  @Test
  void shouldServeObserverOk() {
    prepareGetResponse("/test", "Hello, World!".getBytes());

    val httpBop = new ReqResObserver();
    val client =
        RestClient.forUrl(url)
            .usingApiKey(apiKey)
            .asUserAgent("Bbriccs-Agent")
            .register(httpBop)
            .withTlsVerification(new EmptyTrustManager()) // just for coverage
            .init();
    val request = new HttpBRequest(HttpRequestMethod.GET, "/test");
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

  @Test
  void shouldRethrowSneakyExceptionFromSSLContext() {
    val vauBuilder = RestClient.forUrl(url);
    try (val mockSslContext = mockStatic(SSLContext.class)) {
      mockSslContext
          .when(() -> SSLContext.getInstance("TLS"))
          .thenThrow(NoSuchAlgorithmException.class);
      assertThrows(NoSuchAlgorithmException.class, vauBuilder::withoutTlsVerification);
    }
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
