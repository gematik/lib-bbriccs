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

package de.gematik.bbriccs.fhir.fuzzing;

import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingSessionLogbook;
import java.util.List;
import org.hl7.fhir.r4.model.Resource;

public interface FuzzingEngine {

  /**
   * Apply Fuzzing to the given Resource
   *
   * @param resource to be fuzzed
   * @param <R> generic type-bound of the given Resource
   */
  <R extends Resource> FuzzingSessionLogbook fuzz(R resource);

  FuzzingSessionLogbook getLastSessionLog();

  List<FuzzingSessionLogbook> getSessionHistory();

  // In fact, this is only required for tests...
  FuzzingContext getContext();
}
