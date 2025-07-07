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

package de.gematik.bbriccs.fhir.validation;

import static java.text.MessageFormat.format;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.fhir.validation.utils.FhirValidatingTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.refv.SupportedValidationModule;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class ValidatorFhirTest extends FhirValidatingTest {

  private static ValidatorFhir validator;

  @BeforeAll
  static void setup() {
    validator = ValidatorFhirFactory.createValidator();
  }

  static Stream<Arguments> shouldValidateValidErpFhirResources() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp", true).stream()
        .map(Arguments::of);
  }

  static Stream<Arguments> shouldValidateValidErpFhirResourcesWithRefValidator() {
    val dav =
        ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/dav/1.2", true).stream()
            .filter(f -> f.getName().endsWith(".xml"));
    val kbv =
        ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv/1.1.0", true)
            .stream()
            .filter(f -> f.getName().endsWith(".xml"));
    return Stream.concat(dav, kbv).map(Arguments::of);
  }

  static Stream<Arguments> shouldValidateWithSingleProfileValidatorWithoutSupport() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/hl7", true).stream()
        .map(Arguments::of);
  }

  static Stream<Arguments> shouldValidateValidErpResourcesWithoutCanonicalClaims() {
    return ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/erp/kbv/1.1.0/bundle", true)
        .stream()
        .map(Arguments::of);
  }

  static Stream<Arguments> shouldValidateErpCommunications() {
    return ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/erp/erx/1.2.0/communication", true)
        .stream()
        .map(Arguments::of);
  }

  static Stream<Arguments> shouldDetectInvalidFhirResources() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/invalid", true).stream()
        .map(Arguments::of);
  }

  @Override
  protected void initialize() {
    this.fhirValidator = validator;
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldFailOnValidateGarbage(String content) {
    val vr = assertDoesNotThrow(() -> this.fhirValidator.validate(content));
    assertFalse(vr.isSuccessful());
    assertFalse(vr.getMessages().isEmpty());
    val severity = vr.getMessages().get(0).getSeverity();
    assertThat(severity, anyOf(is(ResultSeverityEnum.ERROR), is(ResultSeverityEnum.FATAL)));
  }

  static Stream<Arguments> shouldFailOnValidateGarbage() {
    return Stream.of(
            "Garbage content is definitely no valid FHIR content",
            "<xml>invalid</xml>",
            "{content: \"invalid\"}",
            "{\"content}\": \"invalid\"}",
            "")
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldValidateValidErpFhirResources(File file) {
    val content = ResourceLoader.readString(file);
    val vr = this.fhirValidator.validate(content);
    printValidationResult(vr);
    assertTrue(
        vr.isSuccessful(), () -> format("Given FHIR-Resource from {0} is invalid", file.getName()));
  }

  @ParameterizedTest
  @MethodSource
  void shouldValidateValidErpFhirResourcesWithRefValidator(File file) {
    val content = ResourceLoader.readString(file);
    val refValidator = ReferenzValidator.withValidationModule(SupportedValidationModule.ERP);
    val vr = refValidator.validate(content);
    printValidationResult(vr);
    assertTrue(
        vr.isSuccessful(), () -> format("Given FHIR-Resource from {0} is invalid", file.getName()));
  }

  @ParameterizedTest
  @MethodSource
  void shouldValidateWithSingleProfileValidatorWithoutSupport(File file) {
    val content = ResourceLoader.readString(file);
    val validator = new ProfiledValidator("test_validator", List.of());

    val vr = validator.validate(content);
    printValidationResult(vr);

    assertTrue(vr.isSuccessful());
    assertNotNull(validator.getContext());
  }

  @ParameterizedTest
  @MethodSource
  void shouldValidateValidErpResourcesWithoutCanonicalClaims(File file) {
    val content = ResourceLoader.readString(file);

    val ctx = FhirContext.forR4();
    val profileSettings = ProfilesConfigurator.getDefaultConfiguration().getProfileConfigurations();
    profileSettings.forEach(
        psdto -> psdto.getProfiles().forEach(pdto -> pdto.setCanonicalClaims(List.of())));
    val customValidator = ValidatorFhirFactory.createValidator(ctx, profileSettings);

    val vr = customValidator.validate(content);
    assertTrue(vr.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource
  void shouldValidateErpCommunications(File file) {
    val content = ResourceLoader.readString(file);

    val vr = this.fhirValidator.validate(content);
    this.printValidationResult(vr);
    assertTrue(vr.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource
  void shouldDetectInvalidFhirResources(File file) {
    val content = ResourceLoader.readString(file);
    val vr = this.fhirValidator.validate(content);
    assertFalse(vr.isSuccessful());
  }
}
