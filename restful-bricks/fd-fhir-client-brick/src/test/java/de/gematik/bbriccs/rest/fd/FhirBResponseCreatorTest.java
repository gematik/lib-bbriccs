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

package de.gematik.bbriccs.rest.fd;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.HttpVersion;
import de.gematik.bbriccs.rest.fd.exceptions.UnexpectedResponseResourceError;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.*;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class FhirBResponseCreatorTest {

  private static FhirCodec fhir;
  private static FhirBResponseCreator responseCreator;
  private static final List<HttpHeader> HEADERS_JSON =
      List.of(new HttpHeader("content-type", MediaType.FHIR_JSON.asString()));

  private static final HttpVersion DEFAULT_HTTP_VERSION = HttpVersion.HTTP_1_1;

  private static final String testToken =
      "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

  @BeforeAll
  static void setUp() {
    fhir = FhirCodec.forR4().andNonProfiledValidator();
    responseCreator =
        new FhirBResponseCreator(fhir, (expect, content) -> fhir.decode(expect, content));
  }

  private String encodeTestRessource(Resource resource, EncodingType type) {
    return fhir.encode(resource, type);
  }

  @ParameterizedTest
  @MethodSource
  void deserializeResourceResponses(String filePath, Class<? extends Resource> resourceType) {
    val content = ResourceLoader.readFileFromResource(filePath);

    val httpResponse = HttpBResponse.status(200).headers(HEADERS_JSON).withPayload(content);
    val response =
        responseCreator
            .expecting(resourceType)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    val auditEvent = response.getAsBaseResource();
    assertNotNull(auditEvent, format("Response must contain a Resource of Type {0}", resourceType));
    assertDoesNotThrow(response::getExpectedResource);
  }

  static Stream<Arguments> deserializeResourceResponses() {
    return Stream.of(
        Arguments.of("examples/fhir/valid/hl7/patient/patient_01.xml", Patient.class),
        Arguments.of(
            "examples/fhir/valid/erp/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml",
            Bundle.class));
  }

  @Test
  void unexpectedOperationOutcomeResponse() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);

    val httpResponse =
        HttpBResponse.status(404).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(OperationOutcome.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    val resource = response.getAsBaseResource();
    assertNotNull(
        resource, format("Response must contain a Resource of Type {0}", OperationOutcome.class));
    assertInstanceOf(
        OperationOutcome.class, resource, "Resource is expected to be OperationOutcome");
    assertTrue(response.isOfExpectedType());

    // get the concrete OperationOutcome
    val concreteResource = response.getAsOperationOutcome();
    assertNotNull(
        concreteResource, format("Resource must be castable to Type {0}", OperationOutcome.class));
  }

  @Test
  void expectedOperationOutcome() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);

    val httpResponse =
        HttpBResponse.status(404).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(OperationOutcome.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    val outputOO = response.getAsBaseResource();
    assertNotNull(
        outputOO, format("Response must contain a Resource of Type {0}", OperationOutcome.class));
    assertInstanceOf(
        OperationOutcome.class, outputOO, "Resource is expected to be OperationOutcome");

    val concreteResource = response.getExpectedResource();
    assertNotNull(
        concreteResource, format("Resource must be castable to Type {0}", OperationOutcome.class));
  }

  @ParameterizedTest
  @MethodSource("deserializeResourceResponses")
  void shouldReceiveResourceAlthoughOperationOutcomeExpected(
      String filePath, Class<? extends Resource> resourceType) {
    val content = ResourceLoader.readFileFromResource(filePath);

    val httpResponse = HttpBResponse.status(201).headers(HEADERS_JSON).withPayload(content);
    val response =
        responseCreator
            .expecting(OperationOutcome.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    assertFalse(response.isResourceOfType(OperationOutcome.class));
    assertTrue(response.isResourceOfType(resourceType));
  }

  @Test
  void shouldFetchUnexpectedResponseResource01() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);

    val httpResponse =
        HttpBResponse.status(404).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @ParameterizedTest
  @MethodSource("deserializeResourceResponses")
  void shouldFetchUnexpectedResponseResource02(
      String filePath, Class<? extends Resource> resourceType) {
    val content = ResourceLoader.readFileFromResource(filePath);

    val expectation = Task.class;
    assertNotEquals(resourceType, expectation); // enusre methodsource does not provide a task

    val httpResponse = HttpBResponse.status(204).headers(HEADERS_JSON).withPayload(content);
    val response =
        responseCreator
            .expecting(expectation)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @Test
  void fetchUnexpectedResponseResourceOptional() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);

    val httpResponse =
        HttpBResponse.status(404).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    val resource = response.getResourceOptional();
    assertTrue(resource.isEmpty());
  }

  @Test
  void shouldIdentifyConcreteResourceOfType() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);

    val httpResponse =
        HttpBResponse.status(404).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    assertTrue(response.isResourceOfType(OperationOutcome.class));
    assertFalse(response.isResourceOfType(AuditEvent.class));
    assertTrue(response.isOperationOutcome());
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @Test
  void shouldValidateEmptyContentCorrectly() {
    val fdRequest = new TestFhirBRequest();
    val httpResponse = HttpBResponse.status(204).headers(HEADERS_JSON).withoutPayload();
    val response =
        responseCreator
            .takeExpectationFrom(fdRequest)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    assertTrue(response.isValidPayload());
  }

  @Test
  void shouldValidateOperationOutcomeCorrectly() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);
    val httpResponse =
        HttpBResponse.status(204).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    assertTrue(response.isValidPayload());
  }

  @Test
  void shouldFailOnInvalidOperationOutcomeCorrectly() {
    val testOperationOutcome =
        encodeTestRessource(createOperationOutcome(), EncodingType.JSON).replace("issue", "issues");
    val httpResponse =
        HttpBResponse.status(204).headers(HEADERS_JSON).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();
    assertFalse(response.isValidPayload());
  }

  @Test
  void shouldCheckHeadersCorrectly() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.JSON);
    val headers = new LinkedList<HttpHeader>();
    headers.add(
        StandardHttpHeaderKey.CONTENT_LENGTH.createHeader(
            String.valueOf(testOperationOutcome.length())));
    headers.addAll(HEADERS_JSON);

    val httpResponse = HttpBResponse.status(404).headers(headers).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    assertTrue(response.isJson());
    assertFalse(response.isXML());
    assertTrue(response.getContentLength() > 0);
    assertFalse(response.isEmptyBody());
    assertTrue(response.toString().contains(OperationOutcome.class.getSimpleName()));
    assertTrue(response.toString().contains("404"));
  }

  @Test
  void shouldCheckHeadersCorrectly02() {
    val testOperationOutcome = encodeTestRessource(createOperationOutcome(), EncodingType.XML);
    val headers = new LinkedList<HttpHeader>();
    headers.add(StandardHttpHeaderKey.CONTENT_TYPE.createHeader(MediaType.FHIR_XML.asString()));

    val httpResponse = HttpBResponse.status(404).headers(headers).withPayload(testOperationOutcome);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    assertFalse(response.isJson());
    assertTrue(response.isXML());
    assertEquals(0, response.getContentLength()); // because content-length header is missing
    assertTrue(response.isEmptyBody());
    assertTrue(response.toString().contains(OperationOutcome.class.getSimpleName()));
    assertTrue(response.toString().contains("404"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "\n", "\n\r", "\r", "\n\n\t"})
  @NullSource
  void shouldThrowOnMissingAnyResponseBody(String emptyBody) {
    val httpResponse =
        HttpBResponse.status(500)
            .version(DEFAULT_HTTP_VERSION)
            .headers(HEADERS_JSON)
            .withPayload(emptyBody);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    assertEquals(EmptyResource.class, response.getResourceType());
    assertTrue(response.isEmptyBody());
    assertTrue(response.isJson());
    assertFalse(response.isXML());
    assertTrue(response.toString().contains(EmptyResource.class.getSimpleName()));
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "\n"})
  @NullSource
  void shouldCreateEmptyResponsesOnEmptyPayloadBody(String payload) {
    val httpResponse =
        HttpBResponse.status(500)
            .version(DEFAULT_HTTP_VERSION)
            .headers(HEADERS_JSON)
            .withPayload(payload);
    val response =
        responseCreator
            .expecting(AuditEvent.class)
            .usedAccessToken(testToken)
            .received(httpResponse)
            .withoutDuration();

    assertEquals(EmptyResource.class, response.getResourceType());
    assertTrue(response.isEmptyBody());
    assertTrue(response.toString().contains(EmptyResource.class.getSimpleName()));
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  private static class TestFhirBRequest extends FhirBaseBRequest<EmptyResource, EmptyResource> {

    protected TestFhirBRequest() {
      super(EmptyResource.class, HttpRequestMethod.POST, "/test");
    }

    @Override
    public EmptyResource getRequestBody() {
      return new EmptyResource();
    }
  }
}
