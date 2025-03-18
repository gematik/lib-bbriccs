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

package de.gematik.bbriccs.fhir.coding.version;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.javafaker.Faker;
import de.gematik.bbriccs.fhir.coding.exceptions.FhirVersionException;
import de.gematik.bbriccs.fhir.coding.utils.TestBasisClassVersion;
import de.gematik.bbriccs.fhir.coding.utils.TestBasisVersion;
import de.gematik.bbriccs.fhir.coding.utils.TestDefaultVersionClass;
import de.gematik.bbriccs.fhir.coding.utils.TestSingleEnumVersion;
import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.StopwatchUtil;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

@Slf4j
class VersionUtilTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(VersionUtil.class));
  }

  @ParameterizedTest(name = "[{index}] Parse Version from {0} and expect {1}")
  @CsvSource(
      value = {"1.0.0:1.0.0", "my.fhir.profile.r4-0.9.4:0.9.4", "4.1:4.1"},
      delimiter = ':')
  void shouldParseVersionStringFromInput(String input, String expected) {
    val version = VersionUtil.parseVersion(input);
    assertEquals(expected, version);
  }

  static Stream<Arguments> shouldParseVersionFromInput() {
    return Stream.of(
        arguments("0.9.13", TestBasisVersion.V0_9_13),
        arguments("test.basis.profile.r4-0.9.13", TestBasisVersion.V0_9_13),
        arguments("1.3.2", TestBasisVersion.V1_3_2));
  }

  @ParameterizedTest(name = "[{index}] Parse Version from {0} and expect {1}")
  @MethodSource
  void shouldParseVersionFromInput(String input, TestBasisVersion expected) {
    val version = VersionUtil.fromString(TestBasisVersion.class, input);
    assertEquals(expected, version);
  }

  static Stream<Arguments> shouldThrowOnInvalidProfileVersionInputs() {
    val inputs = List.of("0.9.14", "test.basis.profile.r4-1.9.13", "abc", "");
    return inputs.stream().map(Arguments::arguments);
  }

  @ParameterizedTest(name = "[{index}] Should throw on invalid profile Version input ''{0}''")
  @MethodSource
  void shouldThrowOnInvalidProfileVersionInputs(String input) {
    assertThrows(
        FhirVersionException.class, () -> VersionUtil.fromString(TestBasisVersion.class, input));
  }

  @Test
  void shouldInstantiateGenericProfileVersion() {
    val v = VersionUtil.fromString(GenericProfileVersion.class, "0.9.8");
    assertEquals("0.9.8", v.getVersion());
  }

  @Test
  void shouldCreateVersionFromNonEnumeratedVersion() {
    val v = VersionUtil.fromString(TestBasisClassVersion.class, "1.0.0");
    assertEquals(0, v.constructorCalls[0]);
    assertEquals(1, v.constructorCalls[1]); // should have called the constructor with parameter!
    assertEquals(0, v.constructorCalls[2]);
  }

  @Test
  void shouldCreateVersionFromVersionClassWithDefaultVersion() {
    val v = VersionUtil.fromString(TestDefaultVersionClass.class, "1.0.0");
    assertEquals(
        1, v.constructorCalls[0]); // should have called the constructor without a parameter!
    assertEquals(0, v.constructorCalls[1]);
    assertEquals(0, v.constructorCalls[2]);
  }

  @Test
  void shouldThrowOnMissmatchOfDefaultVersion() {
    val exception =
        assertThrows(
            FhirVersionException.class,
            () -> VersionUtil.fromString(TestDefaultVersionClass.class, "1.0.1"));
    assertTrue(exception.getMessage().contains("does not match the instantiated"));
  }

  @Test
  void shouldThrowOnInvalidSemver() {
    val exception =
        assertThrows(
            FhirVersionException.class,
            () -> VersionUtil.fromString(GenericProfileVersion.class, "a.b.c"));
    assertTrue(exception.getMessage().contains("does not contain a version"));
  }

  @ParameterizedTest(
      name = "[{index}] Should throw if no constructor found for instantiation on {0}")
  @ValueSource(
      classes = {
        InvalidVersionClass01.class,
        InvalidVersionClass02.class,
        InvalidVersionClass03.class
      })
  <T extends ProfileVersion> void shouldThrowOnMissingConstructor(Class<T> versionClass) {
    val exception =
        assertThrows(
            FhirVersionException.class, () -> VersionUtil.fromString(versionClass, "1.0.0"));
    assertTrue(exception.getMessage().contains("Unable to find a proper Constructor"));
  }

  @Test
  void shouldThrowOnFailingConstructorInvocation() {
    val exception =
        assertThrows(
            FhirVersionException.class,
            () -> VersionUtil.fromString(InvalidVersionClass04.class, "1.0.0"));
    assertTrue(exception.getMessage().contains("Unable to instantiate Version class Constructor"));
  }

  @ParameterizedTest(name = "[{index}] Read MultiDigit Version {0}")
  @ValueSource(strings = {"10.0.0", "1.11.0", "1.0.12", "11.03.0", "11.0.15", "12.16.28"})
  void shouldReadMultiDigitVersions(String version) {
    val v = VersionUtil.fromString(GenericProfileVersion.class, version);
    assertEquals(version, v.getVersion());
  }

  @Test
  void shouldGetSingleEntryEnumAsDefault() {
    val v = VersionUtil.fromString(TestSingleEnumVersion.class, "0.9.13");
    assertEquals(TestSingleEnumVersion.V0_9_13, v);
  }

  @Test
  void shouldThrowOnUnmatchingSingleEnum() {
    val exception =
        assertThrows(
            FhirVersionException.class,
            () -> VersionUtil.fromString(TestSingleEnumVersion.class, "0.9.14"));
    assertTrue(exception.getMessage().contains("version 0.9.14 is not known"));
  }

  @ParameterizedTest(name = "[{index}] Omit Version ZeroPatch from {0} and expect {1}")
  @CsvSource(
      value = {"1.0.0:1.0", "0.9.0:0.9", "4.1.0:4.1", "7.2:7.2"},
      delimiter = ':')
  void shouldOmitZeroPatch(String input, String expected) {
    val version = VersionUtil.omitZeroPatch(input);
    assertEquals(expected, version);
  }

  @ParameterizedTest(name = "[{index}] Omit Version Patch from {0} and expect {1}")
  @CsvSource(
      value = {"my.semver:my.semver", "1.0.1:1.0", "1.9.2:1.9", "4.1.37:4.1", "7.2:7.2"},
      delimiter = ':')
  void shouldOmitPatch(String input, String expected) {
    val version = VersionUtil.omitPatch(input);
    assertEquals(expected, version);
  }

  @ParameterizedTest(name = "[{index}] Do not omit Version NonZeroPatch from {0} and expect {1}")
  @CsvSource(
      value = {"1.0.1:1.0.1", "0.9.2:0.9.2", "4.1.10:4.1.10"},
      delimiter = ':')
  void shouldNotOmitNonZeroPatch(String input, String expected) {
    val version = VersionUtil.omitZeroPatch(input);
    assertEquals(expected, version);
  }

  @ParameterizedTest(
      name = "[{index}] Should compare versions with taking PATCH into account: {0} must equal {1}")
  @CsvSource(
      value = {
        "1.0.0:1.0.0",
        "1.0:1.0.0",
        "1.0.0:1.0",
        "1.0.3:1.0.3",
        "1.0.123:1.0.123",
        "1.0.15:1.0.15",
        "1.2.0:1.2.0",
        "1.2:1.2.0",
        "1.2.0:1.2",
        "1.2.3:1.2.3",
        "1.2.123:1.2.123",
        "1.2.15:1.2.15",
        "10.0.0:10.0.0",
        "10.0:10.0.0",
        "10.0.0:10.0",
        "10.0.3:10.0.3",
        "10.0.123:10.0.123",
        "10.0.15:10.0.15",
        "10.2.0:10.2.0",
        "10.2:10.2.0",
        "10.2.0:10.2",
        "10.2.3:10.2.3",
        "10.2.123:10.2.123",
        "10.2.15:10.2.15",
        "10.2.15.1:10.2.15.1", // edge-case: both are not valid SemVer but equal
        "10.2.15.0:10.2.15.0" // edge-case: both are not valid SemVer but equal
      },
      delimiter = ':')
  void shouldEqualAccountingPatch(String left, String right) {
    val areEqual = VersionUtil.areEqual(left, right);
    assertTrue(areEqual);
  }

  @ParameterizedTest(
      name =
          "[{index}] Should compare versions with taking PATCH into account: {0} must not equal"
              + " {1}")
  @CsvSource(
      value = {
        "1.0.0:1.0.3",
        "1.0:1.0.3",
        "1.0.0:1.0.123",
        "1.0:1.0.123",
        "1.0.0:1.0.15",
        "1.0:1.0.15",
        "1.0.0:1.2.0",
        "1.0:1.2.0",
        "1.0.0:1.2.3",
        "1.0:1.2.3",
        "1.0.0:1.2.123",
        "1.0:1.2.123",
        "1.0.0:1.2.15",
        "1.0:1.2.15",
        "1.0.0:10.0.0",
        "1.0:10.0.0",
        "1.0.0:10.0.3",
        "1.0:10.0.3",
        "1.0.0:10.0.123",
        "1.0:10.0.123",
        "1.0.0:10.0.15",
        "1.0:10.0.15",
        "1.0.0:10.2.0",
        "1.0:10.2.0",
        "1.0.0:10.2.3",
        "1.0:10.2.3",
        "1.0.0:10.2.123",
        "1.0:10.2.123",
        "1.0.0:10.2.15",
        "1.0:10.2.15",
        "1.0.3:1.0.0",
        "1.0.3:1.0.123",
        "1.0.3:1.0.15",
        "1.0.3:1.2.0",
        "1.0.3:1.2.3",
        "1.0.3:1.2.123",
        "1.0.3:1.2.15",
        "1.0.3:10.0.0",
        "1.0.3:10.0.3",
        "1.0.3:10.0.123",
        "1.0.3:10.0.15",
        "1.0.3:10.2.0",
        "1.0.3:10.2.3",
        "1.0.3:10.2.123",
        "1.0.3:10.2.15",
        "1.0.123:1.0.0",
        "1.0.123:1.0.3",
        "1.0.123:1.0.15",
        "1.0.123:1.2.0",
        "1.0.123:1.2.3",
        "1.0.123:1.2.123",
        "1.0.123:1.2.15",
        "1.0.123:10.0.0",
        "1.0.123:10.0.3",
        "1.0.123:10.0.123",
        "1.0.123:10.0.15",
        "1.0.123:10.2.0",
        "1.0.123:10.2.3",
        "1.0.123:10.2.123",
        "1.0.123:10.2.15",
        "1.0.15:1.0.0",
        "1.0.15:1.0.3",
        "1.0.15:1.0.123",
        "1.0.15:1.2.0",
        "1.0.15:1.2.3",
        "1.0.15:1.2.123",
        "1.0.15:1.2.15",
        "1.0.15:10.0.0",
        "1.0.15:10.0.3",
        "1.0.15:10.0.123",
        "1.0.15:10.0.15",
        "1.0.15:10.2.0",
        "1.0.15:10.2.3",
        "1.0.15:10.2.123",
        "1.0.15:10.2.15",
        "1.2.0:1.0.0",
        "1.2:1.0.0",
        "1.2.0:1.0.3",
        "1.2:1.0.3",
        "1.2.0:1.0.123",
        "1.2:1.0.123",
        "1.2.0:1.0.15",
        "1.2:1.0.15",
        "1.2.0:1.2.3",
        "1.2:1.2.3",
        "1.2.0:1.2.123",
        "1.2:1.2.123",
        "1.2.0:1.2.15",
        "1.2:1.2.15",
        "1.2.0:10.0.0",
        "1.2:10.0.0",
        "1.2.0:10.0.3",
        "1.2:10.0.3",
        "1.2.0:10.0.123",
        "1.2:10.0.123",
        "1.2.0:10.0.15",
        "1.2:10.0.15",
        "1.2.0:10.2.0",
        "1.2:10.2.0",
        "1.2.0:10.2.3",
        "1.2:10.2.3",
        "1.2.0:10.2.123",
        "1.2:10.2.123",
        "1.2.0:10.2.15",
        "1.2:10.2.15",
        "1.2.3:1.0.0",
        "1.2.3:1.0.3",
        "1.2.3:1.0.123",
        "1.2.3:1.0.15",
        "1.2.3:1.2.0",
        "1.2.3:1.2.123",
        "1.2.3:1.2.15",
        "1.2.3:10.0.0",
        "1.2.3:10.0.3",
        "1.2.3:10.0.123",
        "1.2.3:10.0.15",
        "1.2.3:10.2.0",
        "1.2.3:10.2.3",
        "1.2.3:10.2.123",
        "1.2.3:10.2.15",
        "1.2.123:1.0.0",
        "1.2.123:1.0.3",
        "1.2.123:1.0.123",
        "1.2.123:1.0.15",
        "1.2.123:1.2.0",
        "1.2.123:1.2.3",
        "1.2.123:1.2.15",
        "1.2.123:10.0.0",
        "1.2.123:10.0.3",
        "1.2.123:10.0.123",
        "1.2.123:10.0.15",
        "1.2.123:10.2.0",
        "1.2.123:10.2.3",
        "1.2.123:10.2.123",
        "1.2.123:10.2.15",
        "1.2.15:1.0.0",
        "1.2.15:1.0.3",
        "1.2.15:1.0.123",
        "1.2.15:1.0.15",
        "1.2.15:1.2.0",
        "1.2.15:1.2.3",
        "1.2.15:1.2.123",
        "1.2.15:10.0.0",
        "1.2.15:10.0.3",
        "1.2.15:10.0.123",
        "1.2.15:10.0.15",
        "1.2.15:10.2.0",
        "1.2.15:10.2.3",
        "1.2.15:10.2.123",
        "1.2.15:10.2.15",
        "10.0.0:1.0.0",
        "10.0:1.0.0",
        "10.0.0:1.0.3",
        "10.0:1.0.3",
        "10.0.0:1.0.123",
        "10.0:1.0.123",
        "10.0.0:1.0.15",
        "10.0:1.0.15",
        "10.0.0:1.2.0",
        "10.0:1.2.0",
        "10.0.0:1.2.3",
        "10.0:1.2.3",
        "10.0.0:1.2.123",
        "10.0:1.2.123",
        "10.0.0:1.2.15",
        "10.0:1.2.15",
        "10.0.0:10.0.3",
        "10.0:10.0.3",
        "10.0.0:10.0.123",
        "10.0:10.0.123",
        "10.0.0:10.0.15",
        "10.0:10.0.15",
        "10.0.0:10.2.0",
        "10.0:10.2.0",
        "10.0.0:10.2.3",
        "10.0:10.2.3",
        "10.0.0:10.2.123",
        "10.0:10.2.123",
        "10.0.0:10.2.15",
        "10.0:10.2.15",
        "10.0.3:1.0.0",
        "10.0.3:1.0.3",
        "10.0.3:1.0.123",
        "10.0.3:1.0.15",
        "10.0.3:1.2.0",
        "10.0.3:1.2.3",
        "10.0.3:1.2.123",
        "10.0.3:1.2.15",
        "10.0.3:10.0.0",
        "10.0.3:10.0.123",
        "10.0.3:10.0.15",
        "10.0.3:10.2.0",
        "10.0.3:10.2.3",
        "10.0.3:10.2.123",
        "10.0.3:10.2.15",
        "10.0.123:1.0.0",
        "10.0.123:1.0.3",
        "10.0.123:1.0.123",
        "10.0.123:1.0.15",
        "10.0.123:1.2.0",
        "10.0.123:1.2.3",
        "10.0.123:1.2.123",
        "10.0.123:1.2.15",
        "10.0.123:10.0.0",
        "10.0.123:10.0.3",
        "10.0.123:10.0.15",
        "10.0.123:10.2.0",
        "10.0.123:10.2.3",
        "10.0.123:10.2.123",
        "10.0.123:10.2.15",
        "10.0.15:1.0.0",
        "10.0.15:1.0.3",
        "10.0.15:1.0.123",
        "10.0.15:1.0.15",
        "10.0.15:1.2.0",
        "10.0.15:1.2.3",
        "10.0.15:1.2.123",
        "10.0.15:1.2.15",
        "10.0.15:10.0.0",
        "10.0.15:10.0.3",
        "10.0.15:10.0.123",
        "10.0.15:10.2.0",
        "10.0.15:10.2.3",
        "10.0.15:10.2.123",
        "10.0.15:10.2.15",
        "10.2.0:1.0.0",
        "10.2:1.0.0",
        "1.0.0:10.2",
        "10.2.0:1.0.3",
        "10.2:1.0.3",
        "10.2.0:1.0.123",
        "10.2:1.0.123",
        "10.2.0:1.0.15",
        "10.2:1.0.15",
        "10.2.0:1.2.0",
        "10.2:1.2.0",
        "10.2.0:1.2.3",
        "10.2:1.2.3",
        "10.2.0:1.2.123",
        "10.2:1.2.123",
        "10.2.0:1.2.15",
        "10.2:1.2.15",
        "10.2.0:10.0.0",
        "10.2:10.0.0",
        "10.2.0:10.0.3",
        "10.2:10.0.3",
        "10.2.0:10.0.123",
        "10.2:10.0.123",
        "10.2.0:10.0.15",
        "10.2:10.0.15",
        "10.2.0:10.2.3",
        "10.2:10.2.3",
        "10.2.0:10.2.123",
        "10.2:10.2.123",
        "10.2.0:10.2.15",
        "10.2:10.2.15",
        "10.2.3:1.0.0",
        "10.2.3:1.0.3",
        "10.2.3:1.0.123",
        "10.2.3:1.0.15",
        "10.2.3:1.2.0",
        "10.2.3:1.2.3",
        "10.2.3:1.2.123",
        "10.2.3:1.2.15",
        "10.2.3:10.0.0",
        "10.2.3:10.0.3",
        "10.2.3:10.0.123",
        "10.2.3:10.0.15",
        "10.2.3:10.2.0",
        "10.2.3:10.2.123",
        "10.2.3:10.2.15",
        "10.2.123:1.0.0",
        "10.2.123:1.0.3",
        "10.2.123:1.0.123",
        "10.2.123:1.0.15",
        "10.2.123:1.2.0",
        "10.2.123:1.2.3",
        "10.2.123:1.2.123",
        "10.2.123:1.2.15",
        "10.2.123:10.0.0",
        "10.2.123:10.0.3",
        "10.2.123:10.0.123",
        "10.2.123:10.0.15",
        "10.2.123:10.2.0",
        "10.2.123:10.2.3",
        "10.2.123:10.2.15",
        "10.2.15:1.0.0",
        "10.2.15:1.0.3",
        "10.2.15:1.0.123",
        "10.2.15:1.0.15",
        "10.2.15:1.2.0",
        "10.2.15:1.2.3",
        "10.2.15:1.2.123",
        "10.2.15:1.2.15",
        "10.2.15:10.0.0",
        "10.2.15:10.0.3",
        "10.2.15:10.0.123",
        "10.2.15:10.0.15",
        "10.2.15:10.2.0",
        "10.2.15:10.2.3",
        "10.2.15:10.2.123",
      },
      delimiter = ':')
  void shouldNotEqualAccountingPatch(String left, String right) {
    val areEqual = VersionUtil.areEqual(left, right);
    assertFalse(areEqual);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"1:1.3.2", "0.9:0.", ".3:1.2", "hello world:1.2.3", "10.2.15.0:10.2.15.1"},
      delimiter = ':')
  void shouldNotEqualOnInvalidSemVer(String left, String right) {
    val areEqual = VersionUtil.areEqual(left, right);
    assertFalse(areEqual);
  }

  @ParameterizedTest(name = "Comparison between {0} and {1} must result {2}")
  @MethodSource
  void shouldCompareVersion(String left, String right, int expectation) {
    val areEqual = VersionUtil.compare(left, right);
    assertEquals(expectation, areEqual);
  }

  static Stream<Arguments> shouldCompareVersion() {
    return Stream.of(
        Arguments.of("1.0.0", "1.0.0", 0),
        Arguments.of("10.20.30", "10.20.30", 0),
        Arguments.of("1.2", "1.2.0", 0),
        Arguments.of("2.1.0", "2.1", 0),
        Arguments.of("2.1.1", "2.1.0", 1),
        Arguments.of("2.1.1", "2.1", 1),
        Arguments.of("3.1.0", "2.1", 1),
        Arguments.of("2.2.0", "2.2.1", -1),
        Arguments.of("2.2", "2.2.1", -1),
        Arguments.of("2.2", "3.2", -1));
  }

  @ParameterizedTest
  @MethodSource
  void shouldCompareBackToBack(String left, String right) {
    val areEqualMeasurement = StopwatchUtil.measure(() -> VersionUtil.areEqual(left, right));
    val compareMeasurement = StopwatchUtil.measure(() -> VersionUtil.compare(left, right) == 0);
    val manualMeasurement =
        StopwatchUtil.measure(
            () -> VersionUtil.omitZeroPatch(left).equals(VersionUtil.omitZeroPatch(right)));

    assertEquals(areEqualMeasurement.response(), compareMeasurement.response());
    assertEquals(compareMeasurement.response(), manualMeasurement.response());

    log.debug(
        format(
            "{0} / {1} / {2}",
            areEqualMeasurement.duration().toNanos(),
            compareMeasurement.duration().toNanos(),
            manualMeasurement.duration().toNanos()));
  }

  static Stream<Arguments> shouldCompareBackToBack() {
    val f = Faker.instance().app();
    // provide n*2 random versions for back2back comparison
    return Stream.of(
        Arguments.of(f.version(), f.version()),
        Arguments.of(f.version(), f.version()),
        Arguments.of(f.version(), f.version()),
        Arguments.of(f.version(), f.version()),
        Arguments.of(f.version(), f.version()),
        Arguments.of(f.version(), f.version()),
        Arguments.of(f.version(), f.version()));
  }

  @ParameterizedTest(
      name = "[{index}] Read Version {0} as default via System Property Configuration")
  @CsvSource(
      value = {"first:1.3.2", "second:0.9.13"},
      delimiter = ':')
  @ClearSystemProperty(key = "bbriccs.fhir.profile.versionutiltest")
  void shouldReadDefaultFromMultiEnumVersionViaSysProp(String profile, String expectedVersion) {
    val toggleName = "bbriccs.fhir.profile.versionutiltest";
    System.setProperty(toggleName, profile);
    // prepares the virtual default configuration
    val profiles = ProfilesConfigurator.getDefaultConfiguration(toggleName);
    val v = VersionUtil.getDefaultVersion(TestBasisVersion.class, "my.profile.r4");
    assertEquals(expectedVersion, v.getVersion());
  }

  @Test
  void shouldReadDefaultFromMultiEnumVersion() {
    // prepares the virtual default configuration
    val profiles = ProfilesConfigurator.getDefaultConfiguration();
    val v = VersionUtil.getDefaultVersion(TestBasisVersion.class, "my.profile.r4");
    assertEquals(TestBasisVersion.V1_3_2, v);
  }

  @Test
  void shouldThrowOnInvalidProfileName() {
    assertThrows(
        FhirVersionException.class,
        () -> VersionUtil.getDefaultVersion(TestBasisVersion.class, "test.profile.r3"));
  }

  @Getter
  private static class InvalidVersionClass01 implements ProfileVersion {

    private final String version;
    private final String name = "invalid.version.class.01";

    public InvalidVersionClass01(String version, boolean test) {
      this.version = version;
    }
  }

  @Getter
  private static class InvalidVersionClass02 implements ProfileVersion {

    private final String version = "1.0.0";
    private final String name = "invalid.version.class.02";
  }

  @Getter
  private static class InvalidVersionClass03 implements ProfileVersion {

    private final String version;
    private final String name;

    private InvalidVersionClass03(String version) {
      this.version = version;
      this.name = "invalid.version.class.03";
    }
  }

  @Getter
  private static class InvalidVersionClass04 implements ProfileVersion {

    private final String version;
    private final String name;

    public InvalidVersionClass04(String version) {
      throw new IllegalArgumentException("for testing purposes only!");
    }
  }
}
