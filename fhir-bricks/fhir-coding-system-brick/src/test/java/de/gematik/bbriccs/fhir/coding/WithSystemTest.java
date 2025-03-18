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

package de.gematik.bbriccs.fhir.coding;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.utils.TestBasisVersion;
import de.gematik.bbriccs.fhir.coding.utils.TestCodeSystem;
import de.gematik.bbriccs.fhir.coding.utils.TestProfileStructureDefinitionEnum;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
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
  void shouldMatchAnyOfTheCanonicalTypes02() {
    val version = TestBasisVersion.V0_9_13;
    val canonicalType = TestProfileStructureDefinitionEnum.TYPE_ONE.asCanonicalType(version);

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_TWO)
            .matches(canonicalType));
  }

  @Test
  void shouldMatchAnyOfTheCanonicalTypesOnMissing() {
    val version = TestBasisVersion.V0_9_13;
    val meta = new Meta();
    meta.setProfile(List.of(TestProfileStructureDefinitionEnum.TYPE_ONE.asCanonicalType(version)));

    assertFalse(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_TWO,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(meta));
  }

  @Test
  void shouldMatchResources() {
    val version = TestBasisVersion.V0_9_13;
    val canonicals =
        Arrays.stream(TestProfileStructureDefinitionEnum.values())
            .map(sd -> sd.asCanonicalType(version))
            .toList();
    val bundle = new Bundle();
    bundle.getMeta().setProfile(canonicals);

    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matches(bundle));
  }

  @Test
  void shouldMatchReferences() {
    val reference =
        new Reference()
            .setIdentifier(
                new Identifier()
                    .setSystem(TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl()));
    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matchesReferenceIdentifier(reference));
    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_THREE,
                TestProfileStructureDefinitionEnum.TYPE_ONE)
            .matchesReferenceIdentifier(reference));
  }

  @Test
  void shouldMatchAnyResources() {
    val version = TestBasisVersion.V0_9_13;
    val bundle = new Bundle();
    bundle
        .getMeta()
        .setProfile(List.of(TestProfileStructureDefinitionEnum.TYPE_ONE.asCanonicalType(version)));

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(bundle));
  }

  @Test
  void shouldMatchBundleEntryComponent() {
    val version = TestBasisVersion.V0_9_13;
    val canonicals =
        Arrays.stream(TestProfileStructureDefinitionEnum.values())
            .map(sd -> sd.asCanonicalType(version))
            .toList();
    val bundle = new Bundle();

    val medication = new Medication();
    medication.getMeta().setProfile(canonicals);
    val bundleEntry = bundle.addEntry().setResource(medication);

    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matches(bundleEntry));
  }

  @Test
  void shouldMatchAnyBundleEntryComponent() {
    val version = TestBasisVersion.V0_9_13;
    val bundle = new Bundle();

    val medication = new Medication();
    medication
        .getMeta()
        .setProfile(List.of(TestProfileStructureDefinitionEnum.TYPE_ONE.asCanonicalType(version)));
    val bundleEntry = bundle.addEntry().setResource(medication);

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(bundleEntry));
  }

  @Test
  void shouldFindInBundleEntries() {
    val version = TestBasisVersion.V0_9_13;
    val canonicals =
        Arrays.stream(TestProfileStructureDefinitionEnum.values())
            .map(sd -> sd.asCanonicalType(version))
            .toList();
    val bundle = new Bundle();

    val medication = new Medication();
    medication.getMeta().setProfile(canonicals);
    bundle.addEntry().setResource(medication);
    bundle.addEntry().setResource(new Medication());
    bundle.addEntry().setResource(new Medication());

    val foundResource =
        TestProfileStructureDefinitionEnum.TYPE_ONE.findFirstResource(bundle.getEntry());
    assertTrue(foundResource.isPresent());
    assertEquals(medication, foundResource.get());
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
  void shouldMatchAnyIdentifier() {
    val identifiers =
        Arrays.stream(TestProfileStructureDefinitionEnum.values())
            .map(t -> new Identifier().setSystem(t.getCanonicalUrl()).setValue(t.name()))
            .toArray(Identifier[]::new);

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(identifiers));
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
  void shouldMatchAnyCodeableConcepts() {
    val coding =
        new Coding(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), "ABC", "Test Code");
    val codeableConcept = new CodeableConcept(coding);

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(codeableConcept));
  }

  @Test
  void shouldMatchAnyCoding() {
    val coding =
        new Coding(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), "ABC", "Test Code");

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(coding));
    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matchesAnyCoding(List.of(coding)));
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

  @Test
  void shouldMatchExtensions() {
    val extension =
        new Extension(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), new StringType("ABC"));

    assertTrue(TestProfileStructureDefinitionEnum.TYPE_ONE.matches(extension));
  }

  @Test
  void shouldMatchAnyExtensions() {
    val extension =
        new Extension(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), new StringType("ABC"));

    assertTrue(
        WithSystem.anyOf(
                TestProfileStructureDefinitionEnum.TYPE_ONE,
                TestProfileStructureDefinitionEnum.TYPE_THREE)
            .matches(extension));
  }

  @Test
  void shouldMatchExtensionsOnDifferentSystems() {
    val extension =
        new Extension(
            TestProfileStructureDefinitionEnum.TYPE_ONE.getCanonicalUrl(), new StringType("ABC"));

    assertFalse(TestProfileStructureDefinitionEnum.TYPE_TWO.matches(extension));
  }
}
