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

package de.gematik.bbriccs.fhir.codec;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.exceptions.FhirCodecException;
import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.GenericProfileVersion;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import de.gematik.bbriccs.fhir.validation.DummyValidator;
import de.gematik.bbriccs.fhir.validation.ValidatorFhirFactory;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.refv.SupportedValidationModule;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class FhirCodecTest extends FhirTest {

  @ParameterizedTest
  @MethodSource
  void shouldDecodeExampleHl7Patients(File file) {
    val content = ResourceLoader.readString(file);
    val patient = assertDoesNotThrow(() -> fhirCodec.decode(Patient.class, content));
    assertEquals("example", patient.getIdPart());

    // ensure decoding multiple times works properly
    val patient2 = assertDoesNotThrow(() -> fhirCodec.decode(Patient.class, content));
    assertEquals(patient.getIdPart(), patient2.getIdPart());
  }

  static Stream<Arguments> shouldDecodeExampleHl7Patients() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/hl7/patient").stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("shouldDecodeExampleHl7Patients")
  void shouldDecodeExampleHl7PatientsWithRefVal(File file) {
    val content = ResourceLoader.readString(file);

    // this one should not have any effects but only for covering the builder
    val codec = FhirCodec.forR4().andReferenzValidator(SupportedValidationModule.ERP);
    val patient = assertDoesNotThrow(() -> codec.decode(Patient.class, content));
    assertEquals("example", patient.getIdPart());

    // ensure decoding multiple times works properly
    val patient2 = assertDoesNotThrow(() -> codec.decode(Patient.class, content));
    assertEquals(patient.getIdPart(), patient2.getIdPart());
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodeExampleHl7PatientsWithoutType(File file) {
    val content = ResourceLoader.readString(file);
    val patient = assertDoesNotThrow(() -> fhirCodec.decode(content));
    assertEquals("example", patient.getIdPart());
    assertEquals(Patient.class, patient.getClass());
  }

  static Stream<Arguments> shouldDecodeExampleHl7PatientsWithoutType() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/hl7/patient", true)
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest(name = "[{index}] Encode Patient with {0}")
  @EnumSource(value = EncodingType.class)
  void shouldEncodeExamples(EncodingType type) {
    val patient = new Patient();
    patient.getMeta().getProfile().add(new CanonicalType("https://gematik.de/test/patient"));

    assertDoesNotThrow(() -> fhirCodec.encode(patient, type));
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodeErpCommunication(File file) {
    val content = ResourceLoader.readString(file);

    val communication = assertDoesNotThrow(() -> fhirCodec.decode(Communication.class, content));
    val basedOn = communication.getBasedOnFirstRep();

    // historical issue: HAPI was putting a / in front of the Task in the reference
    assertFalse(basedOn.getReference().startsWith("/Task"));
  }

  static Stream<Arguments> shouldDecodeErpCommunication() {
    return ResourceLoader.getResourceFilesInDirectory(
            "examples/fhir/valid/erp/erx/1.2.0/communication", true)
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @EnumSource(EncodingType.class)
  @Disabled("This test is not working because the reference is not correctly encoded.")
  void shouldEncodeCommunicationBasedOnCorrectly(EncodingType encodingType) {
    val communication = new Communication();
    communication.addBasedOn(
        new Reference(
            "Task/160.000.033.491.280.78/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea"));

    val content = assertDoesNotThrow(() -> fhirCodec.encode(communication, encodingType));

    // historical issue: HAPI was putting a / in front of the Task in the reference when followed by
    // $accept...
    assertFalse(content.contains("/Task"));
    assertTrue(content.contains("Task/160.000.033.491.280.78"));
  }

  @ParameterizedTest
  @MethodSource
  @Disabled("This test is not working because the reference is not correctly encoded.")
  void shouldEncodeCommunicationBasedOnCorrectlyWithPlainHAPI(IParser parser) {
    val communication = new Communication();
    communication.addBasedOn(new Reference("Task/123123/$accept?ac=ACCESS_CODE"));

    assertEquals(
        "Task/123123/$accept?ac=ACCESS_CODE", communication.getBasedOnFirstRep().getReference());
    val content = parser.encodeResourceToString(communication);

    assertFalse(content.contains("/Task"));
    assertTrue(
        content.contains(
            "Task/123123")); // <-- fails because reference is 123123/$accept?ac=ACCESS_CODE
  }

  static Stream<Arguments> shouldEncodeCommunicationBasedOnCorrectlyWithPlainHAPI() {
    return Stream.of(FhirContext.forR4().newJsonParser(), FhirContext.forR4().newXmlParser())
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodePatientExamplesWithTypeHint(File file) {
    val content = ResourceLoader.readString(file);
    val version = new GenericProfileVersion("1.1.0", false);
    val typedFhir =
        FhirCodec.forR4()
            .withTypeHint(TestKbvStructDef.KBV_PATIENT, version, TestKbvPatient.class)
            .andNonProfiledValidator();

    val patient = assertDoesNotThrow(() -> typedFhir.decode(content));
    assertEquals(TestKbvPatient.class, patient.getClass());
  }

  static Stream<Arguments> shouldDecodePatientExamplesWithTypeHint() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/kbv/1.1.0/patient")
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodePatientExamplesWithTypeHintAndNoVersion(File file) {
    val content = ResourceLoader.readString(file);

    val typedFhir =
        FhirCodec.forR4()
            .withTypeHint(TestKbvStructDef.KBV_PATIENT, TestKbvPatient.class)
            .andNonProfiledValidator();

    val patient = assertDoesNotThrow(() -> typedFhir.decode(content));
    assertEquals(TestKbvPatient.class, patient.getClass());
  }

  static Stream<Arguments> shouldDecodePatientExamplesWithTypeHintAndNoVersion() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/invalid/kbv/patient").stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldDecodeExamplesWithMultipleTypeHints(File file) {
    val content = ResourceLoader.readString(file);

    val version = new GenericProfileVersion("1.1.0", false);
    val typedFhir =
        FhirCodec.forR4()
            .withTypeHint(TestKbvStructDef.KBV_PATIENT, version, TestKbvPatient.class)
            .withTypeHint(TestKbvStructDef.KBV_BUNDLE, version, TestKbvBundle.class)
            .andNonProfiledValidator();

    val bundle = assertDoesNotThrow(() -> typedFhir.decode(TestKbvBundle.class, content));
    assertEquals(TestKbvBundle.class, bundle.getClass());
    assertEquals(TestKbvPatient.class, bundle.getPatient().getClass());
  }

  static Stream<Arguments> shouldDecodeExamplesWithMultipleTypeHints() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv/1.1.0/bundle")
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("shouldDecodeExamplesWithMultipleTypeHints")
  void shouldDecodeExamplesWithMultipleTypeHints02(File file) {
    val content = ResourceLoader.readString(file);

    val version = new GenericProfileVersion("1.1.0", false);
    val typeHints =
        List.of(
            ResourceTypeHint.forStructure(TestKbvStructDef.KBV_PATIENT)
                .forVersion(version)
                .mappingTo(TestKbvPatient.class),
            ResourceTypeHint.forStructure(TestKbvStructDef.KBV_BUNDLE)
                .forVersion(version)
                .mappingTo(TestKbvBundle.class));

    val typedFhir = FhirCodec.forR4().withTypeHints(typeHints).andNonProfiledValidator();

    val bundle = assertDoesNotThrow(() -> typedFhir.decode(TestKbvBundle.class, content));
    assertEquals(TestKbvBundle.class, bundle.getClass());
    assertEquals(TestKbvPatient.class, bundle.getPatient().getClass());
  }

  @ParameterizedTest
  @MethodSource("shouldDecodeExamplesWithMultipleTypeHints")
  void shouldDecodeExamplesWithMultipleTypeHints03(File file) {
    val content = ResourceLoader.readString(file);

    val typeHints =
        List.of(
            ResourceTypeHint.forStructure(TestKbvStructDefTwo.KBV_PATIENT)
                .forAllVersionsFrom(TestKbvVersion.class)
                .mappingTo(TestKbvPatient.class),
            ResourceTypeHint.forStructure(TestKbvStructDefTwo.KBV_BUNDLE)
                .forAllVersionsFrom(TestKbvVersion.class)
                .mappingTo(TestKbvBundle.class));

    val typedFhir = FhirCodec.forR4().withTypeHints(typeHints).andNonProfiledValidator();

    val bundle = assertDoesNotThrow(() -> typedFhir.decode(TestKbvBundle.class, content));
    assertEquals(TestKbvBundle.class, bundle.getClass());
    assertEquals(TestKbvPatient.class, bundle.getPatient().getClass());
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldDecodeEmptyResource(String content) {
    val resource = assertDoesNotThrow(() -> fhirCodec.decode(EmptyResource.class, content));
    assertEquals(EmptyResource.class, resource.getClass());
  }

  static Stream<Arguments> shouldDecodeEmptyResource() {
    return Stream.of("", " ", "\t", "\n", "\r", "\r\n").map(Arguments::of);
  }

  @ParameterizedTest
  @EnumSource(EncodingType.class)
  void shouldEncodeEmptyResource(EncodingType encodingType) {
    val content = assertDoesNotThrow(() -> fhirCodec.encode(new EmptyResource(), encodingType));
    assertTrue(content.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("shouldDecodeExampleHl7Patients")
  void shouldFailOnDecodingContentAsEmptyResource(File file) {
    val content = ResourceLoader.readString(file);
    assertThrows(FhirCodecException.class, () -> fhirCodec.decode(EmptyResource.class, content));
  }

  @ParameterizedTest
  @MethodSource("shouldDecodeExamplesWithMultipleTypeHints")
  void shouldCreateWithConfiguredValidator(File file) {
    val content = ResourceLoader.readString(file);

    FhirCodec profiledCodec;
    try (val validatorFactory = mockStatic(ValidatorFhirFactory.class)) {
      validatorFactory
          .when(ValidatorFhirFactory::createValidator)
          .thenReturn(new DummyValidator(FhirContext.forR4()));
      profiledCodec = FhirCodec.forR4().andBbriccsValidator();
    }

    val bundle = assertDoesNotThrow(() -> profiledCodec.decode(TestKbvBundle.class, content));
    assertNotNull(bundle);
  }

  @Test
  void shouldDisableErrors() {
    val fc = FhirCodec.forR4().disableErrors().andDummyValidator();

    val t = new Task();
    val e = t.addExtension();
    e.setValue(new StringType("Bricks"));
    e.addExtension("https://gematik.de/fhir", new StringType("Bricks"));

    // if errors are not disabled, this will throw because the extension contains both a value and a
    // nested extension
    assertDoesNotThrow(() -> fc.encode(t, EncodingType.XML));
  }

  public static class TestKbvPatient extends Patient {}

  public static class TestKbvBundle extends Bundle {

    public Patient getPatient() {
      return this.getEntry().stream()
          .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Patient))
          .map(
              entry ->
                  (Patient)
                      entry.getResource()) // we could also directly cast to concrete Patient here
          .findFirst()
          .orElseThrow();
    }
  }

  @Getter
  @RequiredArgsConstructor
  public enum TestKbvVersion implements ProfileVersion {
    V_1_1_0("1.1.0");

    private final String version;

    @Override
    public String getName() {
      return "kbv.test";
    }

    @Override
    public boolean omitZeroPatch() {
      return false;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public enum TestKbvStructDef implements WithStructureDefinition<GenericProfileVersion> {
    KBV_PATIENT("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient"),
    KBV_BUNDLE("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle");

    private final String canonicalUrl;
  }

  @Getter
  @RequiredArgsConstructor
  public enum TestKbvStructDefTwo implements WithStructureDefinition<TestKbvVersion> {
    KBV_PATIENT("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient"),
    KBV_BUNDLE("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle");

    private final String canonicalUrl;
  }
}
