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

package de.gematik.bbriccs.fhir.codec.utils;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
public abstract class FhirTest {

  protected FhirCodec fhirCodec;
  protected boolean printEncoded = false;
  protected boolean prettyPrint = false;
  protected EncodingType encodingType = EncodingType.XML;

  @BeforeEach
  void beforeEach() {
    this.initialize();

    if (fhirCodec == null) {
      this.fhirCodec = FhirCodec.forR4().andNonProfiledValidator();
    }
  }

  /**
   * Initialization of a FhirTest CAN initialize its own instance of @see {@link FhirCodec}. By
   * default, a FhirTest will initialize a generic FhirCodec without loading any profiles
   *
   * <p>Additionally, you can change for debugging purposes
   *
   * <ul>
   *   <li>{@link #prettyPrint} (false by default)
   *   <li>{@link #printEncoded} (false by default)
   *   <li>{@link #encodingType} (XML by default)
   * </ul>
   */
  protected void initialize() {}

  protected ValidationResult encodeAndValidate(Resource resource) {
    val encoded = fhirCodec.encode(resource, encodingType, prettyPrint);

    if (printEncoded) printResource(encoded);

    val result = fhirCodec.validate(encoded);
    printValidationResult(result);

    return result;
  }

  protected void printResource(Resource resource) {
    val encoded = fhirCodec.encode(resource, encodingType, prettyPrint);
    printResource(encoded);
  }

  protected void printResource(String resource) {
    System.out.println("\n##########\n" + resource + "\n##########\n");
  }

  protected void printValidationResult(ValidationResult result) {
    printValidationResult(result, m -> !m.getSeverity().equals(ResultSeverityEnum.INFORMATION));
  }

  protected void printValidationResult(
      ValidationResult result, Predicate<SingleValidationMessage> messageFilter) {
    if (!result.isSuccessful()) {
      // give me some hints if the encoded result is invalid
      val r =
          result.getMessages().stream()
              .filter(messageFilter)
              .map(
                  m ->
                      format(
                          "[{0} in Line {3} at {1}]: {2}",
                          m.getSeverity(),
                          m.getLocationString(),
                          m.getMessage(),
                          m.getLocationLine()))
              .collect(Collectors.joining("\n\t"));
      log.warn(
          "--- Found Validation Messages after validation: {} ---\n\t{}\n------",
          result.getMessages().stream().filter(messageFilter).count(),
          r);
    }
  }
}
