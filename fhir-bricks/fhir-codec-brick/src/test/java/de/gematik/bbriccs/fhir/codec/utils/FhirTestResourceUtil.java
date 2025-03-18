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

package de.gematik.bbriccs.fhir.codec.utils;

import static java.text.MessageFormat.format;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class FhirTestResourceUtil {

  private FhirTestResourceUtil() {
    throw new AssertionError();
  }

  public static OperationOutcome createOperationOutcome() {
    // TODO: resolve dependencies and reuse the OperationOutcomeBuilder here!
    val issue = new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setCode(OperationOutcome.IssueType.VALUE);
    issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    issue.getDetails().setText("error details");
    issue.setDiagnostics("additional diagnostics about the error");
    val oo = new OperationOutcome();
    val issueList = new LinkedList<OperationOutcome.OperationOutcomeIssueComponent>();
    issueList.add(issue);
    oo.setIssue(issueList);

    oo.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
    oo.getText().setDivAsString("<div>narrative</div>");
    oo.setId(IdType.newRandomUuid());
    return oo;
  }

  public static ValidationResult createEmptyValidationResult() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(true);
    when(vr.getMessages()).thenReturn(List.of());
    return vr;
  }

  public static ValidationResult createFailingValidationResult() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);
    val errorMessage = new SingleValidationMessage();
    errorMessage.setMessage("mock error message");
    errorMessage.setSeverity(ResultSeverityEnum.ERROR);
    when(vr.getMessages()).thenReturn(List.of(errorMessage));
    return vr;
  }

  public static ValidationResult createFailingValidationResultWithAllSeverities() {
    val vr = mock(ValidationResult.class);
    when(vr.isSuccessful()).thenReturn(false);

    val errorMessages =
        Arrays.stream(ResultSeverityEnum.values)
            .map(
                severity -> {
                  val svm = new SingleValidationMessage();
                  svm.setMessage(format("mock {0} message", severity.getCode()));
                  svm.setSeverity(severity);
                  return svm;
                })
            .toList();

    when(vr.getMessages()).thenReturn(errorMessages);
    return vr;
  }
}
