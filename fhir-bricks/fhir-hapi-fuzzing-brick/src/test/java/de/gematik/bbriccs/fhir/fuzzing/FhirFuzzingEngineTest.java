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

package de.gematik.bbriccs.fhir.fuzzing;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import de.gematik.bbriccs.fhir.fuzzing.exceptions.FuzzerException;
import de.gematik.bbriccs.fhir.fuzzing.impl.FuzzingEngineImpl;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingSessionLogbook;
import de.gematik.bbriccs.fhir.fuzzing.impl.mutators.resources.BundleMutatorProvider;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FhirFuzzingEngineTest extends FhirTest {

  private static final FhirCodec staticFhirCodec =
      FhirCodec.forR4().disableErrors().andDummyValidator();
  private boolean withDebug;

  @Override
  protected void initialize() {
    this.fhirCodec = staticFhirCodec;
    this.withDebug = false;
    this.printEncoded = false;
    this.prettyPrint = false;
  }

  @ParameterizedTest
  @MethodSource
  void shouldFuzzErpExamples(File file) {
    val content = ResourceLoader.readString(file);
    val fuzzer = FuzzingEngineImpl.builder(0.1).withDefaultFuzzers().build();
    val resource = this.fhirCodec.decode(content);

    if (withDebug) {
      try {
        fuzzer.fuzz(resource);
      } catch (Exception e) {
        fail(e.getMessage());
      } finally {
        this.printFuzzingLog(fuzzer.getLastSessionLog());
      }
    } else {
      assertDoesNotThrow(() -> fuzzer.fuzz(resource));
      assertDoesNotThrow(() -> fhirCodec.encode(resource, encodingType, prettyPrint));
    }

    if (this.printEncoded) {
      this.printResource(resource);
    }
  }

  static Stream<Arguments> shouldFuzzErpExamples() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp", true).stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldFuzzFromThinAir(Resource resource) {
    val fuzzer = FuzzingEngineImpl.builder(1.0).withDefaultFuzzers().build();
    assertDoesNotThrow(() -> fuzzer.fuzz(resource));
    assertDoesNotThrow(fuzzer::getLastSessionLog);

    if (withDebug) {
      this.printFuzzingLog(fuzzer.getLastSessionLog());
    }

    if (this.printEncoded) {
      this.printResource(resource);
    }
  }

  static Stream<Arguments> shouldFuzzFromThinAir() {
    return Stream.of(
            new Bundle(),
            new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(new Medication())),
            new Task(),
            new MedicationDispense(),
            new Medication())
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldRegisterCustomResourceMutator(File file) {
    val content = ResourceLoader.readString(file);
    val fuzzer =
        FuzzingEngineImpl.builder(1.0)
            .registerResourceFuzzer(MyBundle.class, MockBundleMutatorProvider::new)
            .registerResourceMutator(MyBundle.class, MockBundleMutatorProvider.getMockMutator())
            .build();
    val resource = this.fhirCodec.decode(MyBundle.class, content);

    assertDoesNotThrow(() -> fuzzer.fuzz(resource));
    assertTrue(MockBundleMutatorProvider.callCounter > 0);
  }

  static Stream<Arguments> shouldRegisterCustomResourceMutator() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv/1.1.0/bundle")
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldRegisterCustomTypeMutator(File file) {
    val content = ResourceLoader.readString(file);
    val fuzzer =
        FuzzingEngineImpl.builder(1.0)
            .registerResourceFuzzer(Bundle.class, BundleMutatorProvider::new)
            .registerTypeFuzzer(Identifier.class, MockIdentifierMutatorProvider::new)
            .registerTypeMutator(Identifier.class, MockIdentifierMutatorProvider.getMockMutator())
            .build();
    val resource = this.fhirCodec.decode(MyBundle.class, content);

    assertDoesNotThrow(() -> fuzzer.fuzz(resource));
    assertDoesNotThrow(() -> fuzzer.fuzz(resource));
    assertDoesNotThrow(() -> fuzzer.fuzz(resource));
    assertTrue(MockIdentifierMutatorProvider.callCounter > 0);
  }

  static Stream<Arguments> shouldRegisterCustomTypeMutator() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv/1.1.0/bundle")
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldRegisterCustomPrimitiveMutator(File file) {
    val content = ResourceLoader.readString(file);
    val fuzzer =
        FuzzingEngineImpl.builder(1.0)
            .withDefaultFuzzers()
            .registerPrimitiveTypeFuzzer(PrimitiveStringTypes.URI, MockPrimitivStringMutator::new)
            .registerPrimitiveMutator(
                PrimitiveStringTypes.URI, MockPrimitivStringMutator.getMockMutator())
            .build();
    val resource = this.fhirCodec.decode(MyBundle.class, content);

    assertDoesNotThrow(() -> fuzzer.fuzz(resource));
    assertTrue(MockPrimitivStringMutator.callCounter > 0);
  }

  static Stream<Arguments> shouldRegisterCustomPrimitiveMutator() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv/1.1.0/bundle")
        .stream()
        .map(Arguments::of);
  }

  @Test
  void shouldCatchMutatorErrors01() {
    FuzzingMutator<Identifier> mutator = (ctx, id) -> FuzzingLogEntry.noop("Exception: " + 7 / 0);

    val fuzzer =
        FuzzingEngineImpl.builder(1.0)
            .registerResourceFuzzer(Bundle.class, BundleMutatorProvider::new)
            .registerTypeFuzzer(Identifier.class, MockIdentifierMutatorProvider::new)
            .registerTypeMutator(Identifier.class, mutator)
            .build();
    val resource = new Bundle();
    resource.setIdentifier(new Identifier());

    val logEntry = assertDoesNotThrow(() -> fuzzer.fuzz(resource));

    if (withDebug) {
      this.printFuzzingLog(logEntry);
    }
  }

  @Test
  void shouldCatchMutatorErrors02() {
    FuzzingMutator<Bundle> mutator = (ctx, id) -> FuzzingLogEntry.noop("Exception: " + 7 / 0);

    val fuzzer =
        FuzzingEngineImpl.builder(1.0)
            .registerResourceFuzzer(Bundle.class, BundleMutatorProvider::new)
            .registerResourceMutator(Bundle.class, mutator)
            .build();
    val resource = new Bundle();
    resource.setIdentifier(new Identifier());

    val logEntry = assertDoesNotThrow(() -> fuzzer.fuzz(resource));

    if (withDebug) {
      this.printFuzzingLog(logEntry);
    }
  }

  @Test
  void shouldThrowOnRegisteringResourceMutatorWithoutFuzzer() {
    val fuzzerBuilder = FuzzingEngineImpl.builder(1.0);

    val mutator = MockBundleMutatorProvider.getMockMutator();
    assertThrows(
        FuzzerException.class,
        () -> fuzzerBuilder.registerResourceMutator(MyBundle.class, mutator));
  }

  @Test
  void shouldThrowOnRegisteringTypeMutatorWithoutFuzzer() {
    val fuzzerBuilder = FuzzingEngineImpl.builder(1.0);

    val mutator = MockIdentifierMutatorProvider.getMockMutator();
    assertThrows(
        FuzzerException.class, () -> fuzzerBuilder.registerTypeMutator(Identifier.class, mutator));
  }

  @Test
  void shouldThrowOnRegisteringPrimitiveMutatorWithoutFuzzer() {
    val fuzzerBuilder = FuzzingEngineImpl.builder(1.0);

    val mutator = MockPrimitivStringMutator.getMockMutator();
    assertThrows(
        FuzzerException.class,
        () -> fuzzerBuilder.registerPrimitiveMutator(PrimitiveStringTypes.URI, mutator));
  }

  @Test
  void shouldThrowOnFuzzingWithoutAnyFuzzers() {
    val fuzzer = FuzzingEngineImpl.builder(1.0).build();
    val bundle = new Bundle();
    assertThrows(FuzzerException.class, () -> fuzzer.fuzz(bundle));
  }

  @Test
  void shouldThrowOnFetchingFromEmtpySessionLog() {
    val fuzzer = FuzzingEngineImpl.builder(1.0).build();
    assertThrows(FuzzerException.class, fuzzer::getLastSessionLog);
    assertEquals(0, fuzzer.getSessionHistory().size());
  }

  @SneakyThrows
  private void printFuzzingLog(FuzzingSessionLogbook sessionLogbook) {
    val mapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    val writer = this.prettyPrint ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();

    val logsOutput = writer.writeValueAsString(sessionLogbook);
    System.out.println(logsOutput);
  }

  public static class MyBundle extends Bundle {}

  @Getter
  public static class MockBundleMutatorProvider implements FhirResourceMutatorProvider<MyBundle> {

    private final List<FuzzingMutator<MyBundle>> mutators = new LinkedList<>();
    private static int callCounter = 0;

    public static FuzzingMutator<MyBundle> getMockMutator() {
      return (ctx, bundle) -> {
        val message = "Call Mock Mutator";
        callCounter++;
        return FuzzingLogEntry.operation(message);
      };
    }
  }

  @Getter
  public static class MockIdentifierMutatorProvider implements FhirTypeMutatorProvider<Identifier> {

    private final List<FuzzingMutator<Identifier>> mutators = new LinkedList<>();
    public static int callCounter = 0;

    public static FuzzingMutator<Identifier> getMockMutator() {
      return (ctx, bundle) -> {
        val message = "Call Mock Mutator";
        callCounter++;
        return FuzzingLogEntry.operation(message);
      };
    }
  }

  @Getter
  public static class MockPrimitivStringMutator implements PrimitiveMutatorProvider<String> {

    private final List<PrimitiveTypeMutator<String>> mutators = new LinkedList<>();
    public static int callCounter = 0;

    public static PrimitiveTypeMutator<String> getMockMutator() {
      return (ctx, bundle) -> {
        val message = "Call Mock Mutator";
        callCounter++;
        return PrimitiveTypeFuzzingResponse.response(
            "Hello World", FuzzingLogEntry.operation(message));
      };
    }
  }
}
