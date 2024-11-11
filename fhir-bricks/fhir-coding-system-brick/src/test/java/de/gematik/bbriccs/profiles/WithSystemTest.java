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

package de.gematik.bbriccs.profiles;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.profiles.utils.TestBasisVersion;
import de.gematik.bbriccs.profiles.utils.TestCodeSystem;
import de.gematik.bbriccs.profiles.utils.TestProfileStructureDefinitionEnum;
import java.util.Arrays;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.junit.jupiter.api.Test;

class WithSystemTest {

  @Test
  void shouldNotMatchOnNullable() {
    assertFalse(TestProfileStructureDefinitionEnum.TYPE_ONE.matches((String) null));
  }

  @Test
  void shouldNotMatchOnEmpty() {
    val url = "";
    assertFalse(TestProfileStructureDefinitionEnum.TYPE_TWO.matches(url));
  }

  @Test
  void shouldMatchAnyOfTheCanonicalTypes() {
    val version = TestBasisVersion.V0_9_13;
    val canonicals =
        Arrays.stream(TestProfileStructureDefinitionEnum.values())
            .map(sd -> sd.asCanonicalType(version))
            .toList();
    val meta = new Meta();
    meta.setProfile(canonicals);

    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matches(meta));
  }

  @Test
  void shouldMatchIdentifier() {
    val identifiers =
        Arrays.stream(TestProfileStructureDefinitionEnum.values())
            .map(t -> new Identifier().setSystem(t.getCanonicalUrl()).setValue(t.name()))
            .toArray(Identifier[]::new);

    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matches(identifiers));
  }

  @Test
  void shouldNotMatchOnDifferentSystems() {
    assertFalse(
        TestProfileStructureDefinitionEnum.TYPE_ONE.matches(
            TestCodeSystem.TYPE_A, TestCodeSystem.TYPE_B));
  }

  @Test
  void shouldMatchCodeableConcepts() {
    val coding =
        new Coding(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), "ABC", "Test Code");
    val codeableConcept = new CodeableConcept(coding);

    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matches(codeableConcept));
  }

  @Test
  void shouldNotMatchCodeableConceptsOnDifferentSystems() {
    val coding1 =
        new Coding(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), "ABC", "Test Code 1");
    val codeableConcept1 = new CodeableConcept(coding1);

    val coding2 =
        new Coding(
            TestProfileStructureDefinitionEnum.TYPE_TWO.getCanonicalUrl(), "DEF", "Test Code 2");
    val codeableConcept2 = new CodeableConcept(coding2);

    assertFalse(
        TestProfileStructureDefinitionEnum.TYPE_THREE.matches(codeableConcept1, codeableConcept2));
  }
}
