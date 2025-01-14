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

package de.gematik.bbriccs.fhir.builder;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.GenericProfileVersion;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class ResourceBuilderTest {

  @Test
  void shouldSetResourceId() {
    val b = new TestBuilder().setId("123");
    assertEquals("123", b.getResourceId());
  }

  @Test
  void shouldGenerateRandomResourceId() {
    val b = new TestBuilder();
    val rid = b.getResourceId();
    assertNotNull(rid);
    assertFalse(rid.isEmpty());

    val rid2 = b.getResourceId();
    assertEquals(rid, rid2);
  }

  @Test
  void shouldThrowOnMissingRequiredValues() {
    val b = new TestBuilder();
    assertThrows(BuilderException.class, () -> b.checkRequired(b.name, "name is missing"));
  }

  @Test
  void shouldThrowOnMissingExactlyOneRequired() {
    val b = new TestBuilder();
    assertThrows(
        BuilderException.class,
        () -> b.checkRequiredExactlyOneOf("name is missing", b.name, b.info));
  }

  @Test
  void shouldThrowOnExceedingExactlyOneRequired() {
    val b = new TestBuilder();
    b.name = "Module";
    b.info = "some info";
    assertThrows(
        BuilderException.class,
        () -> b.checkRequiredExactlyOneOf("name is missing", b.name, b.info));
  }

  @Test
  void shouldPassOnExactlyOne() {
    val b = new TestBuilder();
    b.name = "Module";
    assertDoesNotThrow(() -> b.checkRequiredExactlyOneOf("name is missing", b.name, b.info));
  }

  @Test
  void shouldThrowOnWrongValueSet() {
    val b = new TestBuilder();
    val e =
        assertThrows(
            BuilderException.class,
            () ->
                b.checkValueSet(
                    TestProfileValueSet.AA, TestProfileValueSet.AB, TestProfileValueSet.AC));
    assertTrue(e.getMessage().contains("AA is not in the list"));
    assertTrue(e.getMessage().contains("expected choices: 'AB, AC'"));
  }

  @Test
  void shouldThrowOnEmptyExpectation() {
    val b = new TestBuilder();
    assertThrows(BuilderException.class, () -> b.checkValueSet(TestProfileValueSet.AA));
  }

  @Test
  void shouldNotThrowIfValueSetIsOneOf() {
    val b = new TestBuilder();
    assertDoesNotThrow(
        () ->
            b.checkValueSet(
                TestProfileValueSet.AA,
                TestProfileValueSet.AB,
                TestProfileValueSet.AC,
                TestProfileValueSet.AA));
  }

  @Test
  void shouldThrowIfRequiredListDoesNotHaveEnoughElements() {
    val b = new TestBuilder();
    assertThrows(
        BuilderException.class, () -> b.checkRequiredList(b.bricks, 1, "not enough elements"));
  }

  @Test
  void shouldThrowOnNegativeAmountExpectation() {
    val b = new TestBuilder();
    assertThrows(
        IllegalArgumentException.class,
        () -> b.checkRequiredList(b.bricks, -1, "negative expectation"));
  }

  @Test
  void shouldCastSelf() {
    val b = new TestBuilder();
    val b2 = b.self();
    assertEquals(b, b2);
  }

  @Test
  void shouldBuildTestTask() {
    val task = new TestBuilder().build();
    val meta = task.getMeta();
    assertNotNull(meta);

    val profiles = meta.getProfile();
    assertEquals(1, profiles.size());

    val profile = profiles.get(0);
    assertEquals(TestStructDef.TEST_STRUCT_DEF.getCanonicalUrl(), profile.getValue());
  }

  @Test
  void shouldBuildTestTaskWithVersion() {
    val versionLiteral = "0.8.9";
    val version = new GenericProfileVersion(versionLiteral);
    val task = new TestBuilder().buildWithVersion(version);
    val meta = task.getMeta();
    assertNotNull(meta);

    val profiles = meta.getProfile();
    assertEquals(1, profiles.size());

    val profile = profiles.get(0);
    val versionedProfile =
        format("{0}|{1}", TestStructDef.TEST_STRUCT_DEF.getCanonicalUrl(), versionLiteral);
    assertEquals(versionedProfile, profile.getValue());
  }

  private static class TestBuilder extends ResourceBuilder<Task, TestBuilder> {

    private String name;
    private String info;
    private final List<String> bricks = new ArrayList<>(2);

    @Override
    public Task build() {
      return this.createResource(Task::new, TestStructDef.TEST_STRUCT_DEF);
    }

    public Task buildWithVersion(GenericProfileVersion version) {
      return this.createResource(Task::new, TestStructDef.TEST_STRUCT_DEF, version);
    }
  }

  @Getter
  @RequiredArgsConstructor
  private enum TestStructDef implements WithStructureDefinition<GenericProfileVersion> {
    TEST_STRUCT_DEF("https://gematik.de/fhir/test/StructureDefinition/test");

    private final String canonicalUrl;
  }

  @Getter
  @AllArgsConstructor
  public enum TestCodeSystem implements WithCodeSystem {
    TYPE_A("https://gematik.de/test/CodeSystem/Type-A");

    private final String canonicalUrl;
  }

  @Getter
  @RequiredArgsConstructor
  private enum TestProfileValueSet implements FromValueSet {
    AA("AA", "first value"),
    AB("AB", "second value"),
    AC("AC", "third value");

    private final String code;
    private final String display;

    @Override
    public TestCodeSystem getCodeSystem() {
      return TestCodeSystem.TYPE_A;
    }
  }
}
