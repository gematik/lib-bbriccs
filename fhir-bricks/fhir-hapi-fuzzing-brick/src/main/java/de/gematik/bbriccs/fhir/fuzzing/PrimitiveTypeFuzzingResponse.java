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

package de.gematik.bbriccs.fhir.fuzzing;

import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PrimitiveTypeFuzzingResponse<T> {

  private final T fuzzedValue;
  private final FuzzingLogEntry logEntry;

  public static <T> PrimitiveTypeFuzzingResponse<T> response(
      T fuzzedValue, FuzzingLogEntry logEntry) {
    return new PrimitiveTypeFuzzingResponse<>(fuzzedValue, logEntry);
  }
}