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

import static java.text.MessageFormat.*;

import ca.uhn.fhir.validation.*;
import de.gematik.bbriccs.fhir.ValidationResultHelper;
import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.HttpHeaderKey;
import de.gematik.bbriccs.rest.fd.exceptions.UnexpectedResponseResourceError;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public final class FhirBResponse<R extends Resource> {

  /** The HTTP-Status Code */
  @Getter private final int statusCode;

  @Getter private final Duration duration;
  @Getter private final String usedJwt;

  @Getter private final List<HttpHeader> headers;
  @Nullable private final Resource resource;
  @Getter private final Class<R> expectedType;
  @Getter private final ValidationResult validationResult;

  private FhirBResponse(
      int statusCode,
      Duration duration,
      String usedJwt,
      List<HttpHeader> headers,
      ValidationResult validationResult,
      @Nullable Resource resource,
      Class<R> expectedType) {
    this.statusCode = statusCode;
    this.usedJwt = usedJwt;
    this.headers = headers;
    this.resource = resource;
    this.expectedType = expectedType;
    this.duration = duration;
    this.validationResult = validationResult;
  }

  public static <E extends Resource> FhirBResponseBuilder<E> forPayload(
      Class<E> expectType, @Nullable Resource resource) {
    return new FhirBResponseBuilder<>(resource, expectType);
  }

  public boolean isValidPayload() {
    return this.validationResult.isSuccessful();
  }

  /**
   * This will retrieve the FHIR Payload Resource as the Base-Class without any validation.
   *
   * @return the FHIR Payload as an untyped Resource
   */
  @Nullable
  public Resource getAsBaseResource() {
    return resource;
  }

  public OperationOutcome getAsOperationOutcome() {
    return getResourceAs(OperationOutcome.class);
  }

  public R getExpectedResource() {
    return getResourceAs(this.expectedType);
  }

  /**
   * Extract the Resource from the Response or throw a custom Exception if the Resource is not of
   * the expected type
   *
   * @param errorFunction mapping the ErpResponse to a custom Exception
   * @return the Resource
   * @param <E> the type of the custom exception to be thrown if the Resource is not of the expected
   *     type
   */
  public <E extends RuntimeException> R getExpectedOrThrow(
      Function<FhirBResponse<? extends Resource>, E> errorFunction) {
    return getResourceOptional().orElseThrow(() -> errorFunction.apply(this));
  }

  public Optional<R> getResourceOptional() {
    return getResourceOptional(this.expectedType);
  }

  /**
   * @param clazz
   * @return
   * @param <U>
   */
  private <U extends Resource> U getResourceAs(Class<U> clazz) {
    return getResourceOptional(clazz)
        .orElseThrow(() -> new UnexpectedResponseResourceError(clazz, resource));
  }

  @SuppressWarnings("unchecked")
  public <U extends Resource> Optional<U> getResourceOptional(Class<U> clazz) {
    if (isResourceOfType(clazz)) {
      this.ensureValidationResult();
      return Optional.ofNullable((U) resource);
    } else {
      return Optional.empty();
    }
  }

  public Class<? extends Resource> getResourceType() {
    if (resource != null) {
      return resource.getClass();
    } else {
      return EmptyResource.class;
    }
  }

  public boolean isResourceOfType(Class<? extends Resource> clazz) {
    return clazz.equals(getResourceType());
  }

  public boolean isOfExpectedType() {
    return isResourceOfType(this.expectedType);
  }

  public boolean isOperationOutcome() {
    return OperationOutcome.class.equals(this.getResourceType());
  }

  public boolean isEmptyBody() {
    return this.getContentLength() == 0;
  }

  public String getHeaderValue(HttpHeaderKey key) {
    return this.getHeaderValue(key.getKey());
  }

  public String getHeaderValue(String key) {
    return this.headers.stream()
        .filter(header -> header.key().equalsIgnoreCase(key))
        .map(HttpHeader::value)
        .findFirst()
        .orElse("");
  }

  public MediaType getContentType() {
    return MediaType.fromString(this.getHeaderValue(StandardHttpHeaderKey.CONTENT_TYPE));
  }

  public long getContentLength() {
    val contentLengthHeader = this.getHeaderValue(StandardHttpHeaderKey.CONTENT_LENGTH);
    long contentLength = 0;
    if (!contentLengthHeader.isBlank()) {
      contentLength = Long.parseLong(contentLengthHeader);
    }

    return contentLength;
  }

  public boolean isJson() {
    return this.getContentType() == MediaType.FHIR_JSON;
  }

  public boolean isXML() {
    return this.getContentType() == MediaType.FHIR_XML;
  }

  private void ensureValidationResult() {
    ValidationResultHelper.throwOnInvalidValidationResult(
        this.getResourceType(), this.validationResult);
  }

  @Override
  public String toString() {
    val resourceType = this.getResourceType().getSimpleName();
    return format(
        "FdResponse(rc={0}, payloadType={1}, duration={2})",
        this.getStatusCode(), resourceType, duration.toMillis());
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class FhirBResponseBuilder<E extends Resource> {
    @Nullable private final Resource resource;
    private final Class<E> expectType;
    private int statusCode;
    private String usedJwt;
    private Duration duration = Duration.ZERO;
    private List<HttpHeader> headers = new LinkedList<>();

    public FhirBResponseBuilder<E> withStatusCode(int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public FhirBResponseBuilder<E> withDuration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public FhirBResponseBuilder<E> usedJwt(String jwt) {
      this.usedJwt = jwt;
      return this;
    }

    public FhirBResponseBuilder<E> withHeaders(List<HttpHeader> headers) {
      this.headers.addAll(headers);
      return this;
    }

    public FhirBResponse<E> andValidationResult(ValidationResult vr) {
      return new FhirBResponse<>(statusCode, duration, usedJwt, headers, vr, resource, expectType);
    }
  }
}
