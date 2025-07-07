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

package de.gematik.bbriccs.rest.vau;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.*;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import de.gematik.bbriccs.rest.plugins.HttpBRequestObserver;
import de.gematik.bbriccs.rest.plugins.HttpBResponseObserver;
import de.gematik.bbriccs.rest.tls.EmptyTrustManager;
import de.gematik.bbriccs.rest.vau.exceptions.MissingAuthorizationBearerException;
import de.gematik.bbriccs.rest.vau.exceptions.VauException;
import de.gematik.bbriccs.rest.vau.plugins.VauObserver;
import de.gematik.bbriccs.rest.vau.plugins.VauObserverManager;
import de.gematik.bbriccs.rest.vau.plugins.VauRequestObserver;
import de.gematik.bbriccs.rest.vau.plugins.VauResponseObserver;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.*;
import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.RequestBodyEntity;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VauClient implements HttpBClient {

  private final UnirestInstance unirest;
  private final VauProtocol vauProtocol;
  private final RawHttpCodec rawHttpCodec;
  private final String fdBaseUrl;
  private final List<HttpHeader> staticHeader;

  private final VauObserverManager vauObserver;

  /**
   * UserPseudonym, which will be used for VAU-Sessions. Initially, each VAU-Session starts with
   * UserPseudonym equal to 0
   */
  private String vauUserPseudonym = "0";

  private VauClient(VauClientBuilder builder, VauProtocol vauProtocol) {
    this.fdBaseUrl = Objects.requireNonNull(builder.url, "VauClientBuilder is missing URL");
    this.unirest = Objects.requireNonNull(builder.unirest, "VauClientBuilder is missing Unirest");
    this.staticHeader =
        Objects.requireNonNull(builder.headers, "VauClientBuilder is missing static headers");
    this.rawHttpCodec =
        Objects.requireNonNull(builder.codec, "VauClientBuilder is missing raw HTTP codec");
    this.vauProtocol =
        Objects.requireNonNull(vauProtocol, "VauClientBuilder is missing VAU-Protocol");

    this.vauObserver = builder.observerBuilder.build();
  }

  @Override
  public void shutDown() {
    this.unirest.close();
  }

  public SecretKey symmetricKey() {
    return vauProtocol.getDecryptionKey();
  }

  @Override
  public HttpBResponse send(HttpBRequest bRequest) {
    this.vauObserver.serveRequestObservers(bRequest);
    val rawInnerHttp = rawHttpCodec.encode(bRequest).getBytes(StandardCharsets.UTF_8);

    val bearerToken =
        bRequest.getBearerToken().orElseThrow(MissingAuthorizationBearerException::new);
    val vauEncrypted = vauProtocol.encrypt(bearerToken, rawInnerHttp);
    this.vauObserver.serveRequestObservers(vauEncrypted);
    val vauRequest = this.createRequest(bRequest, vauEncrypted);

    log.info(
        "Send VAU-Request to: {} with Request ID {}",
        getVauRequestUrl(),
        vauEncrypted.requestIdAsString());
    val outerResponse = vauRequest.asBytes();
    val bResponse = createResponse(vauEncrypted, outerResponse);
    this.vauObserver.serveResponseObservers(bResponse);
    log.info(
        "Received VAU-Response with Status Code {} for Request ID {} with"
            + " VAU Userpseudonym: {}",
        outerResponse.getStatus(),
        vauEncrypted.requestIdAsString(),
        vauUserPseudonym);
    return bResponse;
  }

  private HttpBResponse createResponse(
      VauEncryptionEnvelope request, HttpResponse<byte[]> outerResponse) {
    this.extractUserPseudonym(outerResponse);

    // check if the response is octet-stream (encrypted) before decrypting
    val contentType =
        outerResponse.getHeaders().getFirst(StandardHttpHeaderKey.CONTENT_TYPE.getKey());
    val isOctetStream = Objects.requireNonNull(contentType).contains("octet-stream");
    if (isOctetStream) {
      return decryptResponse(request, outerResponse);
    } else {
      return nonEncryptedResponse(contentType, outerResponse);
    }
  }

  private HttpBResponse decryptResponse(
      VauEncryptionEnvelope request, HttpResponse<byte[]> outerResponse) {
    try {
      val decrypted = vauProtocol.decrypt(outerResponse.getBody());
      this.vauObserver.serveResponseObservers(
          new VauEncryptionEnvelope(
              request.vauVersion(),
              request.decryptionKey(),
              request.requestId(),
              request.accessToken(),
              decrypted));
      return rawHttpCodec.decodeResponse(decrypted);
    } catch (BadPaddingException e) {
      val innerHttp = outerResponse.getBody();
      val b64Body = Base64.getEncoder().encodeToString(innerHttp);
      log.error("Error while decoding VAU inner-HTTP of length {}\n{}", innerHttp.length, b64Body);
      throw new VauException("Error while decoding VAU", e);
    }
  }

  /**
   * On rare occasions (usually on remote side errors) the backend sends non-encrypted messages
   * which will be handled here to be at least able to receive such responses
   *
   * @param contentType provided by remote
   * @param outerResponse received from remote
   * @return an extracted HttpBResponse
   */
  private HttpBResponse nonEncryptedResponse(
      String contentType, HttpResponse<byte[]> outerResponse) {
    log.warn(
        "Received VAU Response which seems to be not encrypted with content-type: '{}': forward"
            + " plain content:\n{}",
        contentType,
        new String(outerResponse.getBody()));

    val headers =
        outerResponse.getHeaders().all().stream()
            .map(h -> new HttpHeader(h.getName(), h.getValue()))
            .toList();
    return HttpBResponse.status(outerResponse.getStatus())
        .version(HttpVersion.HTTP_1_1)
        .headers(headers)
        .withPayload(outerResponse.getBody());
  }

  /**
   * Extract the user pseudonym from response and store for next request to maintain the session
   *
   * @param outerResponse received from remote which provides the user pseudonym
   */
  private void extractUserPseudonym(HttpResponse<byte[]> outerResponse) {
    if (outerResponse.getHeaders().containsKey("Userpseudonym")) {
      vauUserPseudonym = outerResponse.getHeaders().getFirst("Userpseudonym");
    } else {
      // reset
      vauUserPseudonym = "0";
    }
  }

  private RequestBodyEntity createRequest(HttpBRequest request, VauEncryptionEnvelope vauEnvelope) {
    val req = this.unirest.post(getVauRequestUrl()).body(vauEnvelope.encrypted());
    this.setHeaders(req, request.headers());
    return req;
  }

  private void setHeaders(RequestBodyEntity req, List<HttpHeader> dynamicHeaders) {
    staticHeader.forEach(
        h -> {
          log.trace("Set static Header '{}' = '{}'", h.key(), h.value());
          req.header(h.key(), h.value());
        });

    dynamicHeaders.forEach(
        h -> {
          log.trace("Set dynamic Header '{}' = '{}'", h.key(), h.value());
          req.header(h.key(), h.value());
        });
  }

  private String getVauRequestUrl() {
    return fdBaseUrl + "/VAU/" + vauUserPseudonym;
  }

  public static VauClientBuilder forUrl(String url) {
    return new VauClientBuilder(url);
  }

  public static class VauClientBuilder {

    private final VauVersion vauVersion = VauVersion.V1;
    private final String url;
    private final List<HttpHeader> headers;
    private final VauObserverManager.VauObserverBuilder observerBuilder;
    private RawHttpCodec codec;
    private UnirestInstance unirest;

    private VauClientBuilder(String url) {
      this.url = url;
      this.observerBuilder = new VauObserverManager.VauObserverBuilder();
      this.headers = new LinkedList<>();

      // add default headers!
      this.headers.add(StandardHttpHeaderKey.CONTENT_TYPE.createHeader("application/octet-stream"));
      this.headers.add(StandardHttpHeaderKey.ACCEPT_CHARSET.createHeader("utf-8"));
      this.headers.add(StandardHttpHeaderKey.ACCEPT.createHeader("application/octet-stream"));
    }

    public VauClientBuilder usingApiKey(String apiKey) {
      this.headers.add(AuthHttpHeaderKey.X_API_KEY.createHeader(apiKey));
      return this;
    }

    public VauClientBuilder asUserAgent(String userAgent) {
      this.headers.add(StandardHttpHeaderKey.USER_AGENT.createHeader(userAgent));
      return this;
    }

    public VauClientBuilder withHeader(String key, String value) {
      return withHeader(new HttpHeader(key, value));
    }

    public VauClientBuilder withHeader(HttpHeader header) {
      this.headers.add(header);
      return this;
    }

    public VauClientBuilder withHeaders(List<HttpHeader> httpHeaders) {
      this.headers.addAll(httpHeaders);
      return this;
    }

    public VauClientBuilder withHttpCodec(RawHttpCodec codec) {
      this.codec = codec;
      return this;
    }

    public VauClientBuilder registerForRequests(HttpBRequestObserver ro) {
      this.observerBuilder.registerForRequests(ro);
      return this;
    }

    public VauClientBuilder registerForRequests(VauRequestObserver vro) {
      this.observerBuilder.registerForRequests(vro);
      return this;
    }

    public VauClientBuilder registerForResponses(HttpBResponseObserver ro) {
      this.observerBuilder.registerForResponses(ro);
      return this;
    }

    public VauClientBuilder registerForResponses(VauResponseObserver vro) {
      this.observerBuilder.registerForResponses(vro);
      return this;
    }

    public VauClientBuilder register(HttpBObserver rro) {
      return this.registerForRequests(rro).registerForResponses(rro);
    }

    public VauClientBuilder registerForVau(VauObserver vro) {
      return this.registerForRequests(vro).registerForResponses(vro);
    }

    public VauClientBuilder withoutTlsVerification() {
      val vauTrustManager = new EmptyTrustManager();
      return this.withTlsVerification(false, vauTrustManager);
    }

    public VauClientBuilder withTlsVerification(X509TrustManager trustManager) {
      return this.withTlsVerification(true, trustManager);
    }

    @SneakyThrows
    private VauClientBuilder withTlsVerification(boolean verifySsl, X509TrustManager trustManager) {
      val sslCtx = SSLContext.getInstance("TLS");
      sslCtx.init(null, new TrustManager[] {trustManager}, new SecureRandom());

      this.unirest = Unirest.spawnInstance();
      this.unirest.config().verifySsl(verifySsl).sslContext(sslCtx);
      return this;
    }

    @SneakyThrows
    public VauClient usingPublicKeyFromRemote() {
      val certUrl = format("{0}/VAUCertificate", this.url);
      val apiKey =
          this.headers.stream()
              .filter(sh -> sh.key().equalsIgnoreCase(AuthHttpHeaderKey.X_API_KEY.getKey()))
              .map(HttpHeader::value)
              .findFirst()
              .orElseThrow();
      val cert = VauCertificateDownload.downloadFrom(certUrl, apiKey);
      return this.usingPublicKey(cert);
    }

    public VauClient usingPublicKey(X509Certificate vauCertificate) {
      return this.usingPublicKey((ECPublicKey) vauCertificate.getPublicKey());
    }

    public VauClient usingPublicKey(@NonNull ECPublicKey publicKey) {
      if (this.unirest == null) {
        this.withoutTlsVerification();
      }

      if (this.codec == null) {
        this.withHttpCodec(RawHttpCodec.defaultCodec());
      }

      val vauProtocol = new VauProtocol(this.vauVersion, publicKey);
      return new VauClient(this, vauProtocol);
    }
  }
}
