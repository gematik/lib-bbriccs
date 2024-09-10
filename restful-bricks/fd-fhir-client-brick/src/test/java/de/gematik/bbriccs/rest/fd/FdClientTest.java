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

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.codec.exceptions.FhirCodecException;
import de.gematik.bbriccs.fhir.validation.DummyValidator;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.fd.exceptions.UnsupportedMediaTypeException;
import de.gematik.bbriccs.rest.fd.plugins.FhirCodecObserver;
import de.gematik.bbriccs.rest.fd.plugins.RequestHeaderProvider;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FdClientTest {

  @ParameterizedTest
  @EnumSource(
      value = MediaType.class,
      names = {"EMPTY", "UNKNOWN"})
  void shouldFailOnBuildingWithUnsupportedMimeTypes(MediaType mt) {
    val fdcb = FdClient.via(null);
    assertThrows(UnsupportedMediaTypeException.class, () -> fdcb.usingFhirMimeType(mt));
  }

  @ParameterizedTest
  @EnumSource(
      value = MediaType.class,
      names = {"FHIR_XML", "FHIR_JSON"})
  void shouldDecodeWithAcceptMime(MediaType mediaType) {
    val content =
        ResourceLoader.readFileFromResource(
            "examples/fhir/valid/erp/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml");
    val httpClient = new TestHttpClient();
    val fdClient =
        FdClient.via(httpClient)
            .usingDefaultFhir(false)
            .acceptingUtf8Charset()
            .usingFhirMimeType(mediaType)
            .build();

    if (mediaType.isEquivalentTo(MediaType.FHIR_XML)) {
      assertDoesNotThrow(() -> fdClient.decode(Bundle.class, content));
    } else {
      assertThrows(FhirCodecException.class, () -> fdClient.decode(Bundle.class, content));
    }
  }

  @ParameterizedTest
  @EnumSource(
      value = MediaType.class,
      names = {"APPLICATION_XML", "APPLICATION_JSON", "FHIR_XML", "FHIR_JSON"})
  void shouldValidateAnyContentType(MediaType mediaType) {
    val content =
        ResourceLoader.readFileFromResource(
            "examples/fhir/valid/erp/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml");
    val httpClient = new TestHttpClient();
    val fdClient =
        FdClient.via(httpClient)
            .usingDefaultFhir(new DummyValidator(FhirContext.forR4()))
            .acceptingUtf8Charset()
            .usingMimeType(mediaType)
            .build();

    assertTrue(fdClient.isValid(content));
  }

  @ParameterizedTest
  @EnumSource(
      value = MediaType.class,
      names = {"FHIR_XML", "FHIR_JSON"})
  void shouldSendRequestViaTestHttpClient(MediaType mediaType) {
    val httpClient = new TestHttpClient();
    val fdClient =
        FdClient.via(httpClient)
            .usingDefaultFhir(false)
            .acceptingUtf8Charset()
            .usingFhirMimeType(mediaType)
            .withHeaderProvider(new TestHeaderProvider())
            .build();

    val response = assertDoesNotThrow(() -> fdClient.request(new TestFdRequest()));
    assertEquals(200, response.getStatusCode());
    assertEquals(1, httpClient.requests.size());

    // provided by the TestHeaderProvider
    assertEquals(
        "Bearer ABC", httpClient.requests.get(0).headerValue(JwtHeaderKey.AUTHORIZATION.getKey()));
    // extracted by the FdClient
    assertEquals("ABC", response.getUsedJwt());
  }

  @ParameterizedTest
  @EnumSource(
      value = MediaType.class,
      names = {"FHIR_XML", "FHIR_JSON"})
  void shouldServeObservers(MediaType mediaType) {
    val observer = new TestFhirCodecObserver();
    val httpClient = new TestHttpClient();
    val fdClient =
        FdClient.via(httpClient)
            .usingDefaultFhir(false)
            .acceptingUtf8Charset()
            .usingFhirMimeType(mediaType)
            .registerForFhir(observer)
            .withHeaderProvider(new TestHeaderProvider())
            .build();

    val response = assertDoesNotThrow(() -> fdClient.request(new TestFdRequest()));
    assertEquals(200, response.getStatusCode());
    assertEquals(1, httpClient.requests.size());

    assertEquals(1, observer.decodeCounter);
    assertEquals(1, observer.encodeCounter);
  }

  private static class TestHttpClient implements HttpBClient {
    private final List<HttpBRequest> requests = new LinkedList<>();

    @Override
    public HttpBResponse send(HttpBRequest bRequest) {
      this.requests.add(bRequest);
      return new HttpBResponse(200, List.of());
    }
  }

  private static class TestFdRequest extends FdBaseRequest<EmptyResource, EmptyResource> {

    protected TestFdRequest() {
      super(EmptyResource.class, HttpRequestMethod.GET, "/Task");
    }

    @Override
    public EmptyResource getRequestBody() {
      return new EmptyResource();
    }
  }

  private static class TestHeaderProvider implements RequestHeaderProvider {

    @Override
    public HttpHeader forRequest(FdRequest<? extends Resource, ? extends Resource> request) {
      return JwtHeaderKey.AUTHORIZATION.createHeader("ABC");
    }
  }

  private static class TestFhirCodecObserver implements FhirCodecObserver {

    private int decodeCounter = 0;
    private int encodeCounter = 0;

    @Override
    public <E extends Resource, R extends Resource> void onDecode(
        Class<E> expectedType, String content, R resource) {
      decodeCounter++;
    }

    @Override
    public <R extends Resource> void onEncode(R resource, String content) {
      encodeCounter++;
    }
  }
}
