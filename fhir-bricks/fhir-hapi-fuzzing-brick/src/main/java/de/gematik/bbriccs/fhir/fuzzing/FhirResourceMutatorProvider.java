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
import org.hl7.fhir.r4.model.Resource;

/**
 * Interface for <a href="https://build.fhir.org/resourcelist.html">list of FHIR-Resources</a>
 * Mutator Providers
 *
 * @param <R> restrict the generic Type of MutatorProviders to subclasses of {@see
 *     org.hl7.fhir.r4.model.Resource}
 */
public interface FhirResourceMutatorProvider<R extends Resource> extends MutatorProvider<R> {

  List<FuzzingMutator<R>> getMutators();
}
