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

package de.gematik.bbriccs.rest.fd.plugins;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.plugins.RequestHeaderProvider;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * This {@see RequestHeaderProvider} is intended to a drop-in replacement for a real header
 * provider. You can use this one during development if you don't have a final implementation but
 * already can fetch a JWT token from somewhere else
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FixedBearerTokenHeaderProvider implements RequestHeaderProvider {

  private final String token;

  public static FixedBearerTokenHeaderProvider withFixedToken(String token) {
    return new FixedBearerTokenHeaderProvider(format("Bearer {0}", token));
  }

  @Nullable
  @Override
  public HttpHeader forRequest(HttpBRequest request) {
    return new HttpHeader("Authorization", token);
  }
}
