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

package de.gematik.bbriccs.fhir.fuzzing.testutils;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingContext;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingEngine;
import de.gematik.bbriccs.fhir.fuzzing.impl.FuzzingEngineImpl;

public abstract class FhirFuzzingMutatorTest extends FhirTest {

  private static final FhirCodec staticFhirCodec = FhirCodec.forR4().andNonProfiledValidator();

  protected FuzzingEngine fuzzer;
  protected FuzzingContext ctx;

  @Override
  protected void initialize() {
    this.fhirCodec = staticFhirCodec;

    this.fuzzer = FuzzingEngineImpl.builder(1.0).withDefaultFuzzers().build();
    this.ctx = this.fuzzer.getContext();
  }
}
