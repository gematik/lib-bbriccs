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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.version.GenericProfileVersion;
import de.gematik.bbriccs.profiles.utils.TestProfileStructureDefinition;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProfileStructureDefinitionTest {

  @ParameterizedTest(
      name = "[{index}] Should get StructureDefinition with Version {1} (from raw {0})")
  @CsvSource(
      value = {"1.0.1:1.0.1", "10.1.1:10.1.1", "11.20.721:11.20.721", "2.A.0:2.A.0"},
      delimiter = ':')
  void shouldGetVersionedSystemUrl(String input, String expected) {
    val version = new GenericProfileVersion(input);
    val tsd = new TestProfileStructureDefinition();

    val plainSystem = tsd.getCanonicalUrl();
    val expectation = format("{0}|{1}", plainSystem, expected);
    assertEquals(expectation, tsd.getVersionedUrl(version));
  }

  @ParameterizedTest(
      name =
          "[{index}] Should get StructureDefinition with omitted patch in Version {1} (from raw"
              + " {0})")
  @CsvSource(
      value = {"1.0.0:1.0", "10.1.0:10.1", "11.20.0:11.20"},
      delimiter = ':')
  void shouldGetVersionedSystemUrlWithOmittedVersionPatch(String input, String expected) {
    val version = new GenericProfileVersion(input);
    val tsd = new TestProfileStructureDefinition();

    val plainSystem = tsd.getCanonicalUrl();
    val expectation = format("{0}|{1}", plainSystem, expected);
    assertEquals(expectation, tsd.getVersionedUrl(version));
  }

  @ParameterizedTest(
      name =
          "[{index}] Should get StructureDefinition without omitted patch in Version {1} (from raw"
              + " {0})")
  @CsvSource(
      value = {"1.0.0:1.0.0", "10.1.0:10.1.0", "11.20.0:11.20.0"},
      delimiter = ':')
  void shouldGetVersionedSystemUrlWithoutOmittedVersionPatch(String input, String expected) {
    val version = new GenericProfileVersion(input, false);
    val tsd = new TestProfileStructureDefinition();

    val plainSystem = tsd.getCanonicalUrl();
    val expectation = format("{0}|{1}", plainSystem, expected);
    assertEquals(expectation, tsd.getVersionedUrl(version));
  }

  @Test
  void shouldGetAsCanonicalTypeWithoutVersion() {
    val tsd = new TestProfileStructureDefinition();
    val ct = tsd.asCanonicalType();

    assertNotNull(ct);
    assertEquals(tsd.getCanonicalUrl(), ct.getValue());
  }

  @Test
  void shouldGetAsVersionedCanonicalType() {
    val version = new GenericProfileVersion("1.0.0");
    val tsd = new TestProfileStructureDefinition();
    val ct = tsd.asCanonicalType(version);

    assertNotNull(ct);
    val expectation = format("{0}|{1}", tsd.getCanonicalUrl(), "1.0");
    assertEquals(expectation, ct.getValue());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldBuildAsBooleanExtension(boolean flag) {
    val tsd = new TestProfileStructureDefinition();
    val e = tsd.asBooleanExtension(flag);

    assertNotNull(e);
    assertEquals(tsd.getCanonicalUrl(), e.getUrl());
    assertEquals(flag, e.getValue().castToBoolean(e.getValue()).booleanValue());
  }
}
