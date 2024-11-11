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

package de.gematik.bbriccs.rest.vau;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.crypto.BC;
import de.gematik.bbriccs.rest.*;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import de.gematik.bbriccs.rest.tls.EmptyTrustManager;
import de.gematik.bbriccs.rest.vau.exceptions.MissingAuthorizationBearerException;
import de.gematik.bbriccs.rest.vau.exceptions.VauException;
import de.gematik.bbriccs.rest.vau.plugins.VauObserver;
import de.gematik.bbriccs.rest.vau.testutils.VauCertificateGenerator;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

@WireMockTest
class VauClientTest {

  @RegisterExtension
  static WireMockExtension wm1 =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  private static String url;
  private X509Certificate vauCertificate;

  @BeforeAll
  static void setup() {
    int port = wm1.getPort();
    url = "http://localhost:" + port;
  }

  @BeforeEach
  void init() throws Exception {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(VauVersion.V1.getCurve()));
    val keyPair = keyPairGenerator.generateKeyPair();
    vauCertificate =
        VauCertificateGenerator.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic());
  }

  @SneakyThrows
  private void prepareEndpointVauCertificate() {
    wm1.stubFor(
        get(urlEqualTo("/VAUCertificate"))
            .willReturn(aResponse().withBody(vauCertificate.getEncoded())));
  }

  @SneakyThrows
  private void prepareEndpointForInvalidVauCertificate(byte[] resBody) {
    wm1.stubFor(get(urlEqualTo("/VAUCertificate")).willReturn(aResponse().withBody(resBody)));
  }

  @SneakyThrows
  private void prepareNegativeNonEncryptedResponse(byte[] resBody, String... resHeader) {
    val headerList = new LinkedList<com.github.tomakehurst.wiremock.http.HttpHeader>();
    for (int i = 0; i < resHeader.length; i += 2) {
      headerList.add(
          com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader(
              resHeader[i], resHeader[i + 1]));
    }
    val headers = new HttpHeaders(headerList);

    wm1.stubFor(
        post(urlEqualTo("/VAU/0"))
            .withHeader("Content-Type", matching("application/octet-stream"))
            .willReturn(aResponse().withStatus(500).withHeaders(headers).withBody(resBody)));
  }

  @SneakyThrows
  private void preparePositiveVauResponse(SecretKey key, String resBody, String... resHeader) {
    val headerLinesBuilder = new StringBuilder();
    for (int i = 0; i < resHeader.length; i += 2) {
      headerLinesBuilder.append(format("{0}: {1}\r\n", resHeader[i], resHeader[i + 1]));
    }

    // TODO: build for testing a proper innerResponseBuilder
    val rb = new StringBuilder();
    rb.append("HTTP/1.1 200 OK\r\n");
    rb.append(headerLinesBuilder);
    rb.append(format("Content-Length: {0}\r\n", resBody.length()));
    rb.append("Content-Type: application/xml\r\n\r\n");
    rb.append(resBody);

    val vauVersion = VauVersion.V1;
    val encrypted =
        vauVersion
            .getSymmetricMethod()
            .encrypt(key, rb.toString().getBytes(StandardCharsets.UTF_8));

    wm1.stubFor(
        post(urlEqualTo("/VAU/0"))
            .withHeader("Content-Type", matching("application/octet-stream"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/octet-stream")
                    .withBody(encrypted)));
  }

  @Test
  void shouldHandleResponse() {
    val vau =
        VauClient.forUrl(url)
            .withHttpCodec(RawHttpCodec.defaultCodec())
            .withHeaders(
                List.of(
                    AuthHttpHeaderKey.X_API_KEY.createHeader("testApiKey"),
                    StandardHttpHeaderKey.USER_AGENT.createHeader("testAgent")))
            .usingPublicKey(vauCertificate);
    vau.init();
    val secret = vau.symmetricKey();

    preparePositiveVauResponse(
        secret,
        "Nobody calls me chicken",
        "Userpseudonym",
        "0",
        "X-Request-Id",
        "testRequestId-123456");

    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token"),
            "What's wrong, McFly? Chicken!");
    val response = vau.send(request);

    assertEquals(200, response.statusCode());
    assertEquals("Nobody calls me chicken", response.bodyAsString());
    assertEquals("testRequestId-123456", response.headerValue("X-Request-Id"));
    assertDoesNotThrow(vau::shutDown);
  }

  @Test
  void shouldThrowOnMissingJwtToken() {
    val vau =
        VauClient.forUrl(url)
            .withHeaders(
                List.of(
                    AuthHttpHeaderKey.X_API_KEY.createHeader("testApiKey"),
                    StandardHttpHeaderKey.USER_AGENT.createHeader("testAgent")))
            .usingPublicKey(vauCertificate);
    vau.init();
    val secret = vau.symmetricKey();

    preparePositiveVauResponse(
        secret,
        "Nobody calls me chicken",
        "Userpseudonym",
        "0",
        "X-Request-Id",
        "testRequestId-123456");

    val request =
        new HttpBRequest(HttpRequestMethod.POST, "/Task", "What's wrong, McFly? Chicken!");
    assertThrows(MissingAuthorizationBearerException.class, () -> vau.send(request));
  }

  @Test
  void shouldHandleUnencryptedResponse500() {
    prepareNegativeNonEncryptedResponse(
        "Nobody calls me chicken".getBytes(StandardCharsets.UTF_8),
        "Userpseudonym",
        "0",
        "content-type",
        "text/plain",
        "X-Request-Id",
        "testRequestId-123456");

    val vau =
        VauClient.forUrl(url)
            .usingApiKey("testApiKey")
            .asUserAgent("Bbriccs-Agent")
            .withHeader("custom-header", "custom-value")
            .usingPublicKey(vauCertificate);
    vau.init();
    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token"),
            "Nobody calls me chicken");
    val response = assertDoesNotThrow(() -> vau.send(request));
    assertEquals("Nobody calls me chicken", response.bodyAsString());
  }

  @Test
  void shouldThrowOnUnencryptedOctetStream() {
    prepareNegativeNonEncryptedResponse(
        "Nobody calls me chicken".getBytes(StandardCharsets.UTF_8),
        "Userpseudonym",
        "0",
        "content-type",
        "application/octet-stream",
        "X-Request-Id",
        "testRequestId-123456");

    val vau =
        VauClient.forUrl(url)
            .withHeader(AuthHttpHeaderKey.X_API_KEY.createHeader("testApiKey"))
            .withHeader(StandardHttpHeaderKey.USER_AGENT.createHeader("testAgent"))
            .usingPublicKey(vauCertificate);
    vau.init();
    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token"),
            "Nobody calls me chicken");
    assertThrows(VauException.class, () -> vau.send(request));
  }

  @Test
  void shouldNotFailIfUserpseudonymMissing() {
    val vau =
        VauClient.forUrl(url)
            .withHeader(AuthHttpHeaderKey.X_API_KEY.createHeader(""))
            .withHeader(StandardHttpHeaderKey.USER_AGENT.createHeader(""))
            .usingPublicKey(vauCertificate);
    vau.init();
    val secret = vau.symmetricKey();
    preparePositiveVauResponse(secret, "Nobody calls me chicken");

    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            List.of(
                new HttpHeader("X-erp-user", "l"),
                JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token")),
            "What's wrong, McFly? Chicken!");
    assertDoesNotThrow(() -> vau.send(request));
  }

  @Test
  void shouldNotFailWithCustomTlsVerification() {
    val vau =
        VauClient.forUrl(url)
            .withHeader(AuthHttpHeaderKey.X_API_KEY.createHeader(""))
            .withHeader(StandardHttpHeaderKey.USER_AGENT.createHeader(""))
            .withTlsVerification(new EmptyTrustManager())
            .usingPublicKey(vauCertificate);
    vau.init();
    val secret = vau.symmetricKey();
    preparePositiveVauResponse(secret, "Nobody calls me chicken");

    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            List.of(
                new HttpHeader("X-erp-user", "l"),
                JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token")),
            "What's wrong, McFly? Chicken!");
    assertDoesNotThrow(() -> vau.send(request));
  }

  @Test
  void shouldNotFailIfUseragentMissing() {
    val vau = VauClient.forUrl(url).usingPublicKey(vauCertificate);
    vau.init();
    val secret = vau.symmetricKey();
    preparePositiveVauResponse(secret, "Nobody calls me chicken");

    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            List.of(
                new HttpHeader("X-erp-user", "l"),
                JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token")),
            "What's wrong, McFly? Chicken!");
    assertDoesNotThrow(() -> vau.send(request));
  }

  @Test
  void shouldServeRegisteredObservers() {
    val httpBop = new ReqResObserver();
    val vau =
        VauClient.forUrl(url)
            .register(httpBop)
            .registerForVau(httpBop)
            .usingPublicKey(vauCertificate);
    vau.init();
    val secret = vau.symmetricKey();
    preparePositiveVauResponse(secret, "Nobody calls me chicken");

    val request =
        new HttpBRequest(
            HttpRequestMethod.POST,
            "/Task",
            List.of(
                new HttpHeader("X-erp-user", "l"),
                JwtHeaderKey.AUTHORIZATION.createHeader("IDP_Token")),
            "What's wrong, McFly? Chicken!");
    val response = assertDoesNotThrow(() -> vau.send(request));

    assertEquals(1, httpBop.requests.size());
    assertEquals(1, httpBop.vauRequests.size());
    assertEquals(1, httpBop.responses.size());
    assertEquals(1, httpBop.vauResponses.size());
  }

  @Test
  void shouldFailIfBaseUrlMissing() {
    val vauBuilder = VauClient.forUrl(null);
    assertThrows(NullPointerException.class, () -> vauBuilder.usingPublicKey(vauCertificate));
  }

  @Test
  void shouldFailIfVauCertificateMissing() {
    val vauBuilder = VauClient.forUrl(url);
    assertThrows(NullPointerException.class, () -> vauBuilder.usingPublicKey((ECPublicKey) null));
  }

  @Test
  void shouldRethrowSneakyExceptionFromSSLContext() {
    val vauBuilder = VauClient.forUrl(url);
    try (val mockSslContext = mockStatic(SSLContext.class)) {
      mockSslContext
          .when(() -> SSLContext.getInstance("TLS"))
          .thenThrow(NoSuchAlgorithmException.class);
      assertThrows(NoSuchAlgorithmException.class, vauBuilder::withoutTlsVerification);
    }
  }

  @Test
  void shouldRetrieveVauCertificateFromRemote() {
    prepareEndpointVauCertificate();
    val vauBuilder = VauClient.forUrl(url).usingApiKey("API-Key");
    assertDoesNotThrow(vauBuilder::usingPublicKeyFromRemote);
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldThrowOnInvalidVauCertificate(byte[] resBody) {
    prepareEndpointForInvalidVauCertificate(resBody);
    val vauBuilder = VauClient.forUrl(url).usingApiKey("API-Key");
    assertThrows(VauException.class, vauBuilder::usingPublicKeyFromRemote);
  }

  static Stream<Arguments> shouldThrowOnInvalidVauCertificate() {
    return Stream.of("HelloWorld".getBytes(StandardCharsets.UTF_8), new byte[0]).map(Arguments::of);
  }

  private static class ReqResObserver implements HttpBObserver, VauObserver {

    private final List<HttpBRequest> requests = new LinkedList<>();
    private final List<VauEncryptionEnvelope> vauRequests = new LinkedList<>();
    private final List<HttpBResponse> responses = new LinkedList<>();
    private final List<VauEncryptionEnvelope> vauResponses = new LinkedList<>();

    @Override
    public void onRequest(HttpBRequest request) {
      this.requests.add(request);
    }

    @Override
    public void onResponse(HttpBResponse response) {
      this.responses.add(response);
    }

    @Override
    public void onRequest(VauEncryptionEnvelope envelope) {
      this.vauRequests.add(envelope);
    }

    @Override
    public void onResponse(VauEncryptionEnvelope envelope) {
      this.vauResponses.add(envelope);
    }
  }
}
