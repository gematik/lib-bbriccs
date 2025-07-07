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

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.rest.exceptions.UnexpectedResponseTypeError;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class ApplicationResponse<P, E> implements HttpBResponse {

  @Delegate private final HttpBResponse response;
  @Nullable private final P responsePayload;
  @Nullable private final E error;

  public Optional<P> asExpected() {
    return Optional.ofNullable(responsePayload);
  }

  public final P asExpectedOrThrow() {
    return asExpected()
        .orElseThrow(() -> new UnexpectedResponseTypeError("payload", response.bodyAsString()));
  }

  public final P asExpectedOrThrow(Function<Throwable, RuntimeException> exceptionFunction) {
    return asExpected()
        .orElseThrow(
            () -> {
              val uerte = new UnexpectedResponseTypeError("payload", response.bodyAsString());
              return exceptionFunction.apply(uerte);
            });
  }

  public final Optional<E> asError() {
    return Optional.ofNullable(error);
  }

  public final E asErrorOrThrow() {
    return asError()
        .orElseThrow(() -> new UnexpectedResponseTypeError("error", response.bodyAsString()));
  }

  public static <P extends ApplicationData, E extends ErrorData> Builder<P, E> wrap(
      ApplicationRequest<P, E> request, HttpBResponse response, ObjectMapper payloadMapper) {
    return new Builder<>(request, response, payloadMapper);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder<P extends ApplicationData, E extends ErrorData> {

    private final ApplicationRequest<P, E> request;
    private final HttpBResponse response;
    private final ObjectMapper objectMapper;

    private <T> Optional<T> parseAs(Class<T> type) {
      try {
        return Optional.of(objectMapper.readValue(response.bodyAsString(), type));
      } catch (Exception e) {
        return Optional.empty();
      }
    }

    private Optional<E> parseAsError() {
      return parseAs(request.errorType());
    }

    @SuppressWarnings("unchecked")
    private Optional<P> parseAsExpectedPayload() {
      if (request.responseType().equals(EmptyApplicationData.class) && response.isEmptyBody()) {
        return (Optional<P>) Optional.of(new EmptyApplicationData());
      }

      val payload =
          request
              .customDecoder()
              .map(d -> d.apply(response))
              .orElseGet(() -> parseAs(request.responseType()).orElse(null));

      return Optional.ofNullable(payload);
    }

    public ApplicationResponse<P, E> create() {
      return to(ApplicationResponse::new);
    }

    public <R extends ApplicationResponse<P, E>> R to(ApplicationResponseBuilder<R, P, E> builder) {
      P payload = null;
      E error = null;

      if (response.statusCode() >= 400) {
        error = parseAsError().orElse(null);
      } else {
        payload = parseAsExpectedPayload().orElse(null);
      }

      if (error == null && payload == null) {
        // TODO: requires a way to provide a builder for custom error type E
        error =
            (E)
                createCustomError(
                    format(
                        "Response payload not properly detected for {0}:\n{1}",
                        response.statusCode(), response.bodyAsString()));
      }
      return builder.create(response, payload, error);
    }

    private static ErrorData createCustomError(String detail) {
      val errorCode = "bricksDecodingError";
      return new ErrorDataDefault(errorCode, detail);
    }
  }

  @FunctionalInterface
  public interface ApplicationResponseBuilder<R, P, E> {
    R create(HttpBResponse response, P payload, E error);
  }
}
