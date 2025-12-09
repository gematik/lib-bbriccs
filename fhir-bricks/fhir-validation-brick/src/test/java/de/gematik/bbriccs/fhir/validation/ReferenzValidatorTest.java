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
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.validation.utils.FhirValidatingTest;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.commons.exceptions.ValidationModuleInitializationException;
import java.util.LinkedList;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;

class ReferenzValidatorTest extends FhirValidatingTest {

  private static final ValidatorFhir MY_VALIDATOR =
      ReferenzValidator.withValidationModule(SupportedValidationModule.ERP);

  @Override
  protected void initialize() {
    this.fhirValidator = MY_VALIDATOR;
  }

  @Test
  void shouldThrowOnInvalidConfiguration() {
    val svm = mock(SupportedValidationModule.class);
    assertThrows(
        ValidationModuleInitializationException.class,
        () -> ReferenzValidator.withValidationModule(svm));
  }

  @ParameterizedTest
  @EnumSource(
      value = BundleType.class,
      mode = Mode.INCLUDE,
      names = {"COLLECTION", "SEARCHSET"})
  void shouldValidateUnprofiledCollectionBundle(BundleType bt) {
    val bundle = new Bundle();
    bundle.setType(bt);

    val amountResources = 2;
    for (var i = 0; i < amountResources; i++) {
      val oop = createOperationOutcome();
      bundle.addEntry().setResource(oop);
    }

    val vr = this.fhirValidator.validate(bundle);
    assertTrue(vr.isSuccessful());

    assertFalse(
        vr.getMessages().isEmpty(), "must contain at least one entry from unprofiled validator");
  }

  @Test
  void shouldValidatingGenericOperationOutcome() {
    val resource = createOperationOutcome();

    val vr = this.fhirValidator.validate(resource);
    assertTrue(vr.isSuccessful());
    assertFalse(vr.getMessages().isEmpty());
    assertTrue(this.fhirValidator.isValid(resource));
  }

  @Test
  void shouldFailOnValidatingUnknownProfile() {
    val resource = createOperationOutcome();
    resource
        .getMeta()
        .addProfile("http://example.com/fhir/StructureDefinition/MY_CUSTOM_OPERATIONE_OUTCOME");

    val vr = this.fhirValidator.validate(resource);
    assertFalse(vr.isSuccessful());
    assertFalse(vr.getMessages().isEmpty());
    assertFalse(this.fhirValidator.isValid(resource));
  }

  static Stream<Arguments> validErpResources() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/fhir/valid/erp/kbv", true);
    return files.stream()
        .map(f -> Arguments.arguments(f.getAbsolutePath(), ResourceLoader.readString(f)));
  }

  @ParameterizedTest(name = "[{index}] Validate valid File ''{0}'' with ReferenzValidator")
  @MethodSource("validErpResources")
  void shouldValidateValidResource(String file, String content) {
    val ctx = this.fhirValidator.getContext();
    val parser =
        EncodingType.guessFromContent(content)
            .chooseAppropriateParser(ctx::newXmlParser, ctx::newJsonParser);
    val resource = parser.parseResource(content);
    assertTrue(this.fhirValidator.isValid(resource));
  }

  @ParameterizedTest(name = "[{index}] Should not throw on invalid File ''{0}''")
  @MethodSource
  void shouldValidateInvalidResourceContents(String file, String content) {
    assertFalse(this.fhirValidator.isValid(content));
  }

  static Stream<Arguments> shouldValidateInvalidResourceContents() {
    val files = ResourceLoader.getResourceFilesInDirectory("examples/invalid", true);
    return files.stream().map(f -> Arguments.arguments(f.getName(), ResourceLoader.readString(f)));
  }

  private static OperationOutcome createOperationOutcome() {
    val issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(OperationOutcome.IssueType.VALUE);
    issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    issue.getDetails().setText("error details");
    issue.setDiagnostics("additional diagnostics about the error");
    val oo = new OperationOutcome();
    val issueList = new LinkedList<OperationOutcomeIssueComponent>();
    issueList.add(issue);
    oo.setIssue(issueList);

    oo.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
    oo.getText().setDivAsString("<div>narrative</div>");
    oo.setId(IdType.newRandomUuid());
    return oo;
  }
}
