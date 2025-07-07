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

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.validation.utils.FhirValidatingTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MultiProfileValidatorTest extends FhirValidatingTest {

  private static final ValidatorFhir MY_VALIDATOR =
      ValidatorFhirFactory.createValidator(FhirContext.forR4());

  static Stream<Arguments> validErpResources() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp", true);
    return files.stream().map(f -> Arguments.arguments(f.getName(), ResourceLoader.readString(f)));
  }

  @Override
  protected void initialize() {
    this.fhirValidator = MY_VALIDATOR;
  }

  @ParameterizedTest(name = "[{index}] Validate valid File ''{0}'' with Bbriccs-Validator")
  @MethodSource("validErpResources")
  void shouldValidateValidResourceContents(String file, String content) {
    val ctx = this.fhirValidator.getContext();
    val parser =
        EncodingType.guessFromContent(content)
            .chooseAppropriateParser(ctx::newXmlParser, ctx::newJsonParser)
            .setOmitResourceId(false)
            .setOverrideResourceIdWithBundleEntryFullUrl(false);

    val resource = parser.parseResource(content);
    val vr = this.fhirValidator.validate(resource);
    this.printValidationResult(vr);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldFilterEmptyProfiles() {
    val content =
        ResourceLoader.readFileFromResource(
            "examples/fhir/valid/erp/erx/1.2.0/acceptbundle/cef4b960-7ce4-4755-b4ce-3b01a30ec2f0.xml");
    val ctx = this.fhirValidator.getContext();
    val parser = ctx.newXmlParser();
    val bundle = parser.parseResource(Bundle.class, content);

    // not very common but technically possible to encode a profile with an empty string
    bundle.getMeta().setProfile(List.of(new CanonicalType("")));

    val vr = this.fhirValidator.validate(bundle);
    assertTrue(vr.isSuccessful());
  }
}
