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

package de.gematik.bbriccs.fhir.de.value;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidSystemException;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class KVNRTest {

  private static final int TEST_REPS = 5;

  @ParameterizedTest(name = "[{index}]: KVNR {0} is structurally invalid")
  @NullSource
  @EmptySource
  @ValueSource(
      strings = {
        "k220645129",
        "ö220645129",
        "Ä220645129",
        "1220645129",
        "A2206451290",
        "B22064512",
        "123"
      })
  void shouldCheckInvalidKvnrFormat(String value) {
    val kvnr = KVNR.from(value);
    assertFalse(kvnr.isValid());
  }

  @ParameterizedTest(name = "[{index}]: KVNR {0} has a invalid check number")
  @ValueSource(strings = {"K220645129", "T012345679", "A005000112", "C000500020"})
  void shouldCheckInvalidKvnrCheckDigit(String value) {
    val kvnr = KVNR.from(value);
    assertFalse(kvnr.isValid());
  }

  @ParameterizedTest(name = "[{index}]: KVNR {0} is valid")
  @ValueSource(strings = {"A000500015", "K220645122", "T012345678", "A000500015", "C000500021"})
  void shouldCheckValidKvnr(String value) {
    val kvnr = KVNR.from(value);
    assertTrue(kvnr.isValid());
  }

  @RepeatedTest(5)
  void shouldGenerateRandomValidKvid() {
    val kvnr = KVNR.random();
    assertTrue(kvnr.isValid());
  }

  @Test
  void shouldGenerateRandomPkv() {
    val kvnr = KVNR.randomPkv();
    assertTrue(kvnr.isValid());
    assertEquals(DeBasisProfilNamingSystem.KVID_PKV_SID.getCanonicalUrl(), kvnr.getSystemUrl());
    assertEquals(InsuranceTypeDe.PKV, kvnr.getInsuranceType());
    assertTrue(kvnr.isPkv());
  }

  @Test
  void shouldGenerateRandomGkv() {
    val kvnr = KVNR.randomGkv();
    assertTrue(kvnr.isValid());
    assertEquals(DeBasisProfilNamingSystem.KVID_GKV_SID.getCanonicalUrl(), kvnr.getSystemUrl());
    assertEquals(InsuranceTypeDe.GKV, kvnr.getInsuranceType());
    assertTrue(kvnr.isGkv());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldCreateAsReference(boolean withCoding) {
    val kvnr = KVNR.randomGkv();
    val reference = kvnr.asReference(withCoding);
    val identifier = reference.getIdentifier();

    assertTrue(DeBasisProfilNamingSystem.KVID_GKV_SID.matches(identifier));
    assertEquals(kvnr.getValue(), identifier.getValue());

    val code = identifier.getType().getCodingFirstRep().getCode();
    if (withCoding) {
      assertEquals(InsuranceTypeDe.GKV.getCode(), code);
    } else {
      assertNull(code);
    }
  }

  @Test
  void shouldCreateAsIdentifier() {
    val kvnr = KVNR.randomGkv();
    val identifier = kvnr.asIdentifier();

    assertTrue(DeBasisProfilNamingSystem.KVID_GKV_SID.matches(identifier));
    val code = identifier.getType().getCodingFirstRep().getCode();
    assertEquals(InsuranceTypeDe.GKV.getCode(), code);
    assertEquals(kvnr.getValue(), identifier.getValue());
  }

  @Test
  void shouldCreateAsIdentifierWithoutDisplay() {
    val kvnr = KVNR.randomGkv();
    val identifier = kvnr.asIdentifier(false);

    assertTrue(DeBasisProfilNamingSystem.KVID_GKV_SID.matches(identifier));
    val code = identifier.getType().getCodingFirstRep().getCode();
    assertNull(code);
  }

  @Test
  void shouldCreateAsIdentifierWithCustomSystem() {
    val kvnr = KVNR.randomGkv();
    val identifier = kvnr.asIdentifier(DeBasisProfilNamingSystem.KVID);

    assertFalse(DeBasisProfilNamingSystem.KVID_GKV_SID.matches(identifier));
    val code = identifier.getType().getCodingFirstRep().getCode();
    assertEquals(InsuranceTypeDe.GKV.getCode(), code);
  }

  @Test
  void shouldThrowOnInvalidSystem() {
    val kvnr = spy(KVNR.randomGkv());
    when(kvnr.getSystem()).thenReturn(DeBasisProfilNamingSystem.IKNR);

    assertThrows(InvalidSystemException.class, kvnr::getInsuranceType);
  }

  @Test
  void shouldGetFromIdentifier() {
    val originalKvnr = KVNR.random();
    val newKvnr = KVNR.from(originalKvnr.asIdentifier());
    assertEquals(originalKvnr.getValue(), newKvnr.getValue());
    assertEquals(originalKvnr.getSystem(), newKvnr.getSystem());
  }

  @Test
  void shouldThrowOnGettingFromIdentifier() {
    val pznIdentifier = PZN.random().asIdentifier();
    assertThrows(InvalidSystemException.class, () -> KVNR.from(pznIdentifier));
  }

  @RepeatedTest(value = TEST_REPS)
  void shouldExtractFromIdentifier() {
    val originalKvnr = KVNR.random();
    val identifier = originalKvnr.asIdentifier();

    val kvnr = KVNR.extractFrom(identifier);
    assertTrue(kvnr.isPresent());
    assertEquals(originalKvnr, kvnr.get());
  }

  @RepeatedTest(value = TEST_REPS)
  void shouldExtractFromIdentifiers() {
    val originalKvnr = KVNR.random();
    val kvnrIdentifier = originalKvnr.asIdentifier();
    val iknrIdentifier = IKNR.random().asIdentifier();
    val pznIdentifier = PZN.random().asIdentifier();

    val kvnr = KVNR.extractFrom(List.of(pznIdentifier, iknrIdentifier, kvnrIdentifier));
    assertTrue(kvnr.isPresent());
    assertEquals(originalKvnr, kvnr.get());
  }

  @RepeatedTest(value = TEST_REPS)
  void shouldThrowOnMissingKvnrInIdentifiers() {
    val iknrIdentifier = IKNR.random().asIdentifier();
    val pznIdentifier = PZN.random().asIdentifier();
    val identifiers = List.of(pznIdentifier, iknrIdentifier);

    assertThrows(
        MissingFieldException.class, () -> KVNR.extractFromOrThrow(Task.class, identifiers));
  }

  @RepeatedTest(value = TEST_REPS)
  void shouldThrowOnInvalidIdentifier() {
    val iknrIdentifier = IKNR.random().asIdentifier();
    assertThrows(
        MissingFieldException.class, () -> KVNR.extractFromOrThrow(Task.class, iknrIdentifier));
  }

  @RepeatedTest(value = TEST_REPS)
  void shouldNotThrowOnValidIdentifier() {
    val identifier = KVNR.random().asIdentifier();
    assertDoesNotThrow(() -> KVNR.extractFromOrThrow(Task.class, identifier));
  }
}
