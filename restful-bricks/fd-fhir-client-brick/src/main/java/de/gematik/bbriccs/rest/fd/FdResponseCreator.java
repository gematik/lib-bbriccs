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
 */

package de.gematik.bbriccs.rest.fd;

import static java.text.MessageFormat.*;

import ca.uhn.fhir.parser.*;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.exceptions.FhirCodecException;
import de.gematik.bbriccs.rest.HttpBResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.*;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class FdResponseCreator {

  private final FhirCodec fhir;
  private final BiFunction<Class<? extends Resource>, String, Resource> decoder;

  public FdResponseCreator(
      FhirCodec fhir, BiFunction<Class<? extends Resource>, String, Resource> decoder) {
    this.fhir = fhir;
    this.decoder = decoder;
  }

  public <T extends Resource, R extends Resource> FdResponseBuilder<R> takeExpectationFrom(
      FdRequest<T, R> request) {
    return expecting(request.expectedResponseType());
  }

  public <R extends Resource> FdResponseBuilder<R> expecting(Class<R> expectResponseType) {
    return new FdResponseBuilder<>(this.fhir, this.decoder, expectResponseType);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class FdResponseBuilder<R extends Resource> {
    private final FhirCodec fhir;
    private final BiFunction<Class<? extends Resource>, String, Resource> decoder;
    private final Class<R> expectResponseType;
    @Nullable private String usedAccessToken;
    @Nullable private HttpBResponse httpResponse;

    public FdResponseBuilder<R> usedAccessToken(String accessToken) {
      this.usedAccessToken = accessToken;
      return this;
    }

    public FdResponseBuilder<R> received(HttpBResponse httpResponse) {
      this.httpResponse = httpResponse;
      return this;
    }

    public FdResponse<R> withoutDuration() {
      return withDuration(Duration.ZERO);
    }

    public FdResponse<R> withDuration(Duration duration) {
      Objects.requireNonNull(
          this.usedAccessToken, "No AccessToken which was used for the Request provided");
      Objects.requireNonNull(this.httpResponse, "No HTTP-Response provided");
      if (httpResponse.statusCode() >= 500) {
        // log unhandled errors from backend for better analysis
        log.error(
            format(
                "Server Error {0}: {1}", httpResponse.statusCode(), httpResponse.bodyAsString()));
      }

      val vr = this.validateContent(httpResponse.bodyAsString());
      val resource = this.decode(httpResponse.bodyAsString(), expectResponseType);
      return FdResponse.forPayload(expectResponseType, resource)
          .withStatusCode(httpResponse.statusCode())
          .withDuration(duration)
          .usedJwt(usedAccessToken)
          .withHeaders(httpResponse.headers())
          .andValidationResult(vr);
    }

    private Resource decode(String content, Class<? extends Resource> expect) {
      log.trace("Try to decode FHIR Content as {}\n{}", expect.getSimpleName(), content);
      Resource ret;
      try {
        ret = this.decoder.apply(expect, content);
      } catch (FhirCodecException | DataFormatException | IllegalArgumentException e) {
        // try to decode without an expected class (and let HAPI decide) as this case may occur:
        // 1. DataFormatException happens if the Backend responds with an OperationOutcome (or any
        // other unexpected resource) while another resource was expected
        // 2. IllegalArgumentException is thrown if an empty response is expected, but we still get
        // a resource (probably an OperationOutcome)
        log.info(
            format(
                "Given content of length {0} could not be decoded as {1}, try without expectation",
                content.length(), expect.getSimpleName()));
        // although we assume an operation outcome here, let HAPI decide on the concrete type by
        // providing no information (null) about the expected type
        ret = this.decoder.apply(null, content);
      }
      return ret;
    }

    private ValidationResult validateContent(String content) {
      ValidationResult vr;
      if (content.isEmpty() || content.isBlank()) {
        // create an empty validation results which will always be successful
        vr = new ValidationResult(this.fhir.getContext(), List.of());
      } else {
        vr = this.fhir.validate(content);
        if (!vr.isSuccessful()) {
          log.error(format("FHIR Content is invalid\n{0}", content));
        }
      }

      return vr;
    }
  }
}
