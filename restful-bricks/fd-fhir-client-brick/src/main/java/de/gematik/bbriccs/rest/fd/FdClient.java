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

package de.gematik.bbriccs.rest.fd;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import de.gematik.bbriccs.fhir.validation.ValidatorFhirFactory;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.fd.exceptions.UnsupportedMediaTypeException;
import de.gematik.bbriccs.rest.fd.plugins.*;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class FdClient {

  private final HttpBClient httpClient;
  @Getter private final FhirCodec fhir;
  private final FdResponseCreator responseCreator;
  private final List<RequestHeaderProvider> headerProviders;
  private final String acceptCharset;
  private final MediaType acceptMime;
  private final MediaType sendMime;
  private final EncodingType encodingType;
  private final EncodingType decodingType;
  private final FhirCodecObserverManager fhirObserver;

  private FdClient(FdClientBuilder builder) {
    this.httpClient =
        Objects.requireNonNull(builder.httpClient, "FdClient is missing underlying HTTP-transport");
    this.fhir = Objects.requireNonNull(builder.fhir, "FdClient is missing FHIR-Codec");
    this.acceptCharset =
        Objects.requireNonNull(builder.acceptCharset, "FdClient is missing Accept-Charset");
    this.acceptMime =
        Objects.requireNonNull(builder.acceptMime, "FdClient is missing Accept-Mime-Type");
    this.sendMime = Objects.requireNonNull(builder.sendMime, "FdClient is missing Send-Mime-Type");

    this.responseCreator = new FdResponseCreator(this.fhir, this::decode);
    this.headerProviders = builder.headerProviders;

    this.encodingType = this.sendMime.toFhirEncoding();
    this.decodingType = this.acceptMime.toFhirEncoding();
    this.fhirObserver = builder.fhirObserverBuilder.build();
  }

  public <R extends Resource> String encode(R resource) {
    return this.encode(resource, false);
  }

  public <R extends Resource> String encode(R resource, boolean prettyPrint) {
    val ret = this.fhir.encode(resource, encodingType, prettyPrint);
    this.fhirObserver.serveEncoderObservers(resource, ret);
    return ret;
  }

  public <R extends Resource> R decode(Class<R> type, String content) {
    val ret = this.fhir.decode(type, content, decodingType);
    this.fhirObserver.serveDecoderObservers(type, content, ret);
    return ret;
  }

  public boolean isValid(String content) {
    return this.validate(content).isSuccessful();
  }

  public ValidationResult validate(String content) {
    return this.fhir.validate(content);
  }

  public <T extends Resource, R extends Resource> FdResponse<R> request(FdRequest<T, R> request) {
    val body = this.encode(request.getRequestBody());

    // set static and dynamic headers
    val httpHeaders = initHeaders();
    httpHeaders.addAll(request.getHeaders());
    this.headerProviders.forEach(provider -> httpHeaders.add(provider.forRequest(request)));

    val httpRequest =
        new HttpBRequest(request.getMethod(), request.getRequestLocator(), httpHeaders, body);

    val start = Instant.now();
    val httpResponse = this.httpClient.send(httpRequest);
    val duration = Duration.between(start, Instant.now());
    val idpToken = httpRequest.getBearerToken().orElse("n/a");

    return this.responseCreator
        .takeExpectationFrom(request)
        .usedAccessToken(idpToken)
        .received(httpResponse)
        .withDuration(duration);
  }

  private List<HttpHeader> initHeaders() {
    val headers = new LinkedList<HttpHeader>();
    headers.add(StandardHttpHeaderKey.ACCEPT_CHARSET.createHeader(this.acceptCharset));
    headers.add(StandardHttpHeaderKey.ACCEPT.createHeader(this.acceptMime.asString()));
    headers.add(StandardHttpHeaderKey.CONTENT_TYPE.createHeader(this.sendMime.asString()));

    return headers;
  }

  public static FdClientBuilder via(HttpBClient httpClient) {
    return new FdClientBuilder(httpClient);
  }

  public static class FdClientBuilder {
    private final HttpBClient httpClient;
    private final List<RequestHeaderProvider> headerProviders = new LinkedList<>();
    private final FhirCodecObserverManager.FhirObserverBuilder fhirObserverBuilder =
        new FhirCodecObserverManager.FhirObserverBuilder();
    private FhirCodec fhir;

    private String acceptCharset = "utf-8";
    private MediaType acceptMime;
    private MediaType sendMime;

    private FdClientBuilder(HttpBClient httpClient) {
      this.httpClient = httpClient;
    }

    public FdClientBuilder usingFhir(FhirCodec fhir) {
      this.fhir = fhir;
      return this;
    }

    public FdClientBuilder usingDefaultFhir(boolean withValidation) {
      if (withValidation) return this.usingDefaultFhir(ValidatorFhirFactory.createValidator());
      else return usingFhir(FhirCodec.forR4().andDummyValidator());
    }

    public FdClientBuilder usingDefaultFhir(ValidatorFhir fhirValidator) {
      return usingFhir(FhirCodec.forR4().andCustomValidator(fhirValidator));
    }

    public FdClientBuilder acceptingUtf8Charset() {
      return acceptingCharset("utf-8");
    }

    public FdClientBuilder acceptingCharset(String acceptCharset) {
      this.acceptCharset = acceptCharset;
      return this;
    }

    public FdClientBuilder acceptMimeType(MediaType mimeType) {
      this.acceptMime = mimeType;
      return this;
    }

    public FdClientBuilder sendMimeType(MediaType mimeType) {
      this.sendMime = mimeType;
      return this;
    }

    public FdClientBuilder usingMimeType(MediaType mimeType) {
      return this.sendMimeType(mimeType).acceptMimeType(mimeType);
    }

    public FdClientBuilder usingFhirMimeType(MediaType mimeType) {
      return switch (mimeType) {
        case ACCEPT_FHIR_XML, FHIR_XML -> this.sendMimeType(MediaType.FHIR_XML)
            .acceptMimeType(MediaType.ACCEPT_FHIR_XML);
        case ACCEPT_FHIR_JSON, FHIR_JSON -> this.sendMimeType(MediaType.FHIR_JSON)
            .acceptMimeType(MediaType.ACCEPT_FHIR_JSON);
        default -> throw new UnsupportedMediaTypeException(mimeType);
      };
    }

    public FdClientBuilder withHeaderProvider(RequestHeaderProvider provider) {
      this.headerProviders.add(provider);
      return this;
    }

    public FdClientBuilder registerForFhirEncode(FhirEncoderObserver feo) {
      this.fhirObserverBuilder.registerForEncode(feo);
      return this;
    }

    public FdClientBuilder registerForFhirDecode(FhirDecoderObserver fdo) {
      this.fhirObserverBuilder.registerForDecode(fdo);
      return this;
    }

    public FdClientBuilder registerForFhir(FhirCodecObserver fco) {
      return this.registerForFhirDecode(fco).registerForFhirEncode(fco);
    }

    public FdClient build() {
      return new FdClient(this);
    }
  }
}
