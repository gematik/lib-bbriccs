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

package de.gematik.bbriccs.fhir.fuzzing.impl;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntryType;
import de.gematik.bbriccs.fhir.fuzzing.testutils.FhirFuzzingMutatorTest;
import lombok.NoArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.Test;

class FuzzingContextImplTest extends FhirFuzzingMutatorTest {

  @Test
  void shouldResponseWithNoopOnNullType01() {
    DateTimeType type = null;
    val fle = this.ctx.fuzzChild("test", type);
    assertEquals(FuzzingLogEntryType.NOOP, fle.getType());
  }

  @Test
  void shouldResponseWithNoopOnNullType02() {
    DateTimeType type = null;
    val fle = this.ctx.fuzzChild(Bundle.class, type);
    assertEquals(FuzzingLogEntryType.NOOP, fle.getType());
  }

  @Test
  void shouldResponseWithNoopOnNullType03() {
    DateTimeType type = null;
    val fle = this.ctx.fuzzIdElement(DateTimeType.class, type);
    assertEquals(FuzzingLogEntryType.NOOP, fle.getType());
  }

  @Test
  void shouldResponseWithNoopOnNullType04() {
    Bundle bundle = null;
    val fle = this.ctx.fuzzIdElement(Bundle.class, bundle);
    assertEquals(FuzzingLogEntryType.NOOP, fle.getType());
  }

  @Test
  void shouldResponseWithNoopOnBundleWithoutResources() {
    val bundle = new Bundle();
    val fle = this.ctx.fuzzChildResources(bundle);
    assertEquals(FuzzingLogEntryType.NOOP, fle.getType());
  }

  @Test
  void shouldFuzzDeeplyInheritedResources() {
    val deepBundle = new MyBundleThree();
    val fle = assertDoesNotThrow(() -> this.ctx.startFuzzingSession(deepBundle));
    assertFalse(fle.isEmpty());
  }

  @Test
  void shouldFuzzDeeplyInheritedTypes() {
    val deepType = new MyDateTimeTypeThree();
    val fle = assertDoesNotThrow(() -> this.ctx.fuzzChild("test", deepType));
    assertNotNull(fle);
  }

  static class MyBundleOne extends Bundle {}

  static class MyBundleTwo extends MyBundleOne {}

  static class MyBundleThree extends MyBundleTwo {}

  @NoArgsConstructor
  static class MyDateTimeTypeOne extends DateTimeType {}

  @NoArgsConstructor
  static class MyDateTimeTypeTwo extends MyDateTimeTypeOne {}

  @NoArgsConstructor
  static class MyDateTimeTypeThree extends MyDateTimeTypeTwo {}
}
