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

package de.gematik.bbriccs.fhir.fuzzing.testutils;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingContext;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingEngine;
import de.gematik.bbriccs.fhir.fuzzing.impl.FuzzingContextImpl;
import de.gematik.bbriccs.fhir.fuzzing.impl.FuzzingEngineImpl;
import de.gematik.bbriccs.fhir.fuzzing.impl.rnd.ProbabilityDiceImpl;
import de.gematik.bbriccs.fhir.fuzzing.impl.rnd.RandomnessImpl;
import java.security.SecureRandom;
import lombok.val;

public abstract class FhirFuzzingMutatorTest extends FhirTest {

  private static final FhirCodec staticFhirCodec = FhirCodec.forR4().andNonProfiledValidator();

  protected FuzzingEngine fuzzer;
  protected FuzzingContext ctx;

  @Override
  protected void initialize() {
    this.fhirCodec = staticFhirCodec;

    val rnd = new SecureRandom();
    val probability = 1.0;
    val pdMutator = new ProbabilityDiceImpl(rnd, probability);
    val pdChildResources = new ProbabilityDiceImpl(rnd, probability);
    val randomness = new RandomnessImpl(rnd, pdMutator, pdChildResources);
    this.ctx = new FuzzingContextImpl(randomness);

    this.fuzzer = FuzzingEngineImpl.builder(1.0).withDefaultFuzzers().build();
  }
}
