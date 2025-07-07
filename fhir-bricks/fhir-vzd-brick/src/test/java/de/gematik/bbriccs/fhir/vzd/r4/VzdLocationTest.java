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

package de.gematik.bbriccs.fhir.vzd.r4;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.utils.FhirTest;
import de.gematik.bbriccs.fhir.vzd.VzdFhirCodeFactory;
import de.gematik.bbriccs.fhir.vzd.util.VzdSummaryPrinter;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VzdLocationTest extends FhirTest {

  private final VzdSummaryPrinter printer = new VzdSummaryPrinter();

  @Override
  protected void initialize() {
    this.fhirCodec = VzdFhirCodeFactory.withoutValidation();
  }

  @ParameterizedTest(name = "Read Example {0} with explicit type hin")
  @MethodSource("getLocationExamples")
  void shouldSummarizeLocations(String filename, String content) {
    val resource = this.fhirCodec.decode(VzdLocation.class, content);
    assertDoesNotThrow(() -> printer.printSummary(resource));
  }

  @ParameterizedTest(name = "Read Example {0} with implicit type hint")
  @MethodSource("getLocationExamples")
  void shouldDecodeWithoutExplicitTypeHint(String filename, String content) {
    val resource = this.fhirCodec.decode(content);
    val vzdResource = assertInstanceOf(VzdLocation.class, resource);
    assertDoesNotThrow(() -> printer.printSummary(vzdResource));
  }

  @ParameterizedTest
  @CsvSource(
      value = {"null, null", "null, 123", "123, null"},
      nullValues = {"null"})
  void shouldNotThrowOnMissingGeoLocation(Long longitude, Long latitude) {
    val location = new VzdLocation();
    val position = location.getPosition();
    Optional.ofNullable(longitude).ifPresent(position::setLongitude);
    Optional.ofNullable(latitude).ifPresent(position::setLatitude);

    val locString = assertDoesNotThrow(location::getLocationString);
    assertTrue(locString.contains("n/a"));
  }

  static Stream<Arguments> getLocationExamples() {
    return ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/vzd/location/").stream()
        .map(f -> Arguments.of(f.getName(), ResourceLoader.readString(f)));
  }
}
