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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationClientImpl implements ApplicationClient {

  private final HttpBClient httpBClient;
  private final ObjectMapper objectMapper;

  @Override
  public <P extends ApplicationData, E extends ErrorData, R extends ApplicationRequest<P, E>>
      ApplicationResponse<P, E> request(R request) {
    val response = httpBClient.send(request);
    return ApplicationResponse.wrap(request, response, objectMapper).create();
  }

  @Override
  public <P extends ApplicationData, E extends ErrorData, R extends ApplicationRequest<P, E>>
      P requestExpectedOrThrow(R request) {
    val response = this.request(request);

    if (response.asExpected().isEmpty()) {
      log.error(
          "Error on Request {}: payload is NOT of type {}",
          request.getClass().getSimpleName(),
          request.responseType().getSimpleName());
    }
    return response.asExpectedOrThrow();
  }

  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  public static class Builder {
    private final HttpBClient httpBClient;

    public ApplicationClient withSimpleObjectMapper() {
      return withObjectMapper(new ObjectMapper());
    }

    public ApplicationClient withObjectMapper(ObjectMapper objectMapper) {
      val om = Optional.ofNullable(objectMapper).orElseGet(ObjectMapper::new);
      return new ApplicationClientImpl(httpBClient, om);
    }
  }
}
