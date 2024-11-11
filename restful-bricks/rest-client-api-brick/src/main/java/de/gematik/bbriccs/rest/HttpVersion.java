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

package de.gematik.bbriccs.rest;

import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/** Enum for HTTP versions */
@RequiredArgsConstructor
public enum HttpVersion {
  /** HTTP version 1.1 */
  HTTP_1_1("HTTP/1.1"),

  /** HTTP version 2 */
  HTTP_2("HTTP/2");

  public final String version;

  @Override
  public String toString() {
    return version;
  }

  public static HttpVersion fromString(String version) {
    return optionalFromString(version)
        .orElseThrow(() -> new IllegalArgumentException("Unknown HTTP version: " + version));
  }

  public static Optional<HttpVersion> optionalFromString(String version) {
    return Arrays.stream(values()).filter(v -> v.version.equalsIgnoreCase(version)).findFirst();
  }
}
