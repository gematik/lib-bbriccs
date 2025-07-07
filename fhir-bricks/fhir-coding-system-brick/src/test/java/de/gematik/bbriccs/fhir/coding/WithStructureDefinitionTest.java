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

package de.gematik.bbriccs.fhir.coding;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.utils.TestProfileStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.GenericProfileVersion;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.UrlType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WithStructureDefinitionTest {

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
          "[{index}] Should get StructureDefinition with omitted patch in Version {2} (from raw"
              + " {1})")
  @CsvSource(
      value = {
        "true:12.30.40:12.30",
        "false:1.0.0:1.0",
        "false:10.1.0:10.1",
        "false:11.20.0:11.20",
        "false:12.30.40:12.30.40"
      },
      delimiter = ':')
  void shouldGetVersionedSystemUrlWithOmittedVersionPatch(
      boolean omitPatch, String input, String expected) {
    val version = new GenericProfileVersion("generic.fhir.r4", input, true, omitPatch);
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
    assertInstanceOf(BooleanType.class, e.getValue());
    assertEquals(flag, e.getValue().castToBoolean(e.getValue()).booleanValue());
  }

  @Test
  void shouldBuildAsStringExtension() {
    val tsd = new TestProfileStructureDefinition();
    val extensionValue = "Hello World";
    val e = tsd.asStringExtension(extensionValue);

    assertNotNull(e);
    assertEquals(tsd.getCanonicalUrl(), e.getUrl());
    assertInstanceOf(StringType.class, e.getValue());
    assertEquals(extensionValue, e.getValue().castToString(e.getValue()).getValue());
  }

  @Test
  void shouldBuildAsCodeExtension() {
    val tsd = new TestProfileStructureDefinition();
    val extensionValue = "Hello World";
    val e = tsd.asCodeExtension(extensionValue);

    assertNotNull(e);
    assertEquals(tsd.getCanonicalUrl(), e.getUrl());
    assertInstanceOf(CodeType.class, e.getValue());
    assertEquals(extensionValue, e.getValue().castToCode(e.getValue()).getValue());
  }

  @Test
  void shouldBuildAsUrlExtension() {
    val tsd = new TestProfileStructureDefinition();
    val extensionValue = "https://example.com";
    val e = tsd.asUrlExtension(extensionValue);

    assertNotNull(e);
    assertEquals(tsd.getCanonicalUrl(), e.getUrl());
    assertInstanceOf(UrlType.class, e.getValue());
    assertEquals(extensionValue, e.getValue().castToUrl(e.getValue()).getValue());
  }

  @Test
  void shouldBuildAsUriExtension() {
    val tsd = new TestProfileStructureDefinition();
    val extensionValue = "mailto:John.Doe@example.com";
    val e = tsd.asUriExtension(extensionValue);

    assertNotNull(e);
    assertEquals(tsd.getCanonicalUrl(), e.getUrl());
    assertInstanceOf(UriType.class, e.getValue());
    assertEquals(extensionValue, e.getValue().castToUri(e.getValue()).getValue());
  }

  @Test
  void shouldBuildAsEmptyExtension() {
    val tsd = new TestProfileStructureDefinition();
    val e = tsd.asExtension();

    assertNotNull(e);
    assertEquals(tsd.getCanonicalUrl(), e.getUrl());
    assertNull(e.getValue());
  }
}
