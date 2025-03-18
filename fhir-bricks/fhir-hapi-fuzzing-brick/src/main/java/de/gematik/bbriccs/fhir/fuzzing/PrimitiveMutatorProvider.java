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

import java.util.List;

/**
 * Interface for primitive <a href="https://build.fhir.org/datatypes.html">FHIR-Types</a>
 *
 * @param <T> restrict the generic Type of MutatorProviders to a specific primitive type, e.g.,
 *     String, int, double etc.
 */
public interface PrimitiveMutatorProvider<T> {

  List<PrimitiveTypeMutator<T>> getMutators();
}
