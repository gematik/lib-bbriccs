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

package de.gematik.bbriccs.fhir.builder;

import static java.text.MessageFormat.format;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.val;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;

public class OperationOutcomeBuilder
    extends ResourceBuilder<OperationOutcome, OperationOutcomeBuilder> {

  private final List<OperationOutcome.OperationOutcomeIssueComponent> issues = new LinkedList<>();

  private Narrative.NarrativeStatus narrativeStatus;
  private String narrativeText;

  public static OperationOutcomeBuilder create() {
    return new OperationOutcomeBuilder();
  }

  public OperationOutcomeBuilder addIssue(OperationOutcome.OperationOutcomeIssueComponent issue) {
    issues.add(issue);
    return self();
  }

  public IssueBuilder withIssue() {
    return new IssueBuilder(this::addIssue);
  }

  public OperationOutcomeBuilder narrativeText(String text) {
    return narrativeText(text, Narrative.NarrativeStatus.GENERATED);
  }

  public OperationOutcomeBuilder narrativeText(String text, Narrative.NarrativeStatus status) {
    this.narrativeText = text;
    this.narrativeStatus = status;
    return self();
  }

  @Override
  public OperationOutcome build() {
    val oo = new OperationOutcome();

    oo.setId(this.getResourceId());
    oo.setIssue(issues);

    Optional.ofNullable(this.narrativeText)
        .ifPresent(
            t -> {
              oo.getText().setDivAsString(format("<div>{0}</div>", t));
              oo.getText().setStatus(this.narrativeStatus);
            });

    return oo;
  }

  public static class IssueBuilder {
    private final Function<OperationOutcome.OperationOutcomeIssueComponent, OperationOutcomeBuilder>
        finalizer;
    private final OperationOutcome.OperationOutcomeIssueComponent issue =
        new OperationOutcome.OperationOutcomeIssueComponent();

    public IssueBuilder(
        Function<OperationOutcome.OperationOutcomeIssueComponent, OperationOutcomeBuilder>
            finalizer) {
      this.finalizer = finalizer;
      this.issue.setCode(OperationOutcome.IssueType.VALUE);
      this.issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
    }

    public IssueBuilder severity(OperationOutcome.IssueSeverity severity) {
      this.issue.setSeverity(severity);
      return this;
    }

    public IssueBuilder diagnostics(String text) {
      this.issue.setDiagnostics(text);
      return this;
    }

    public OperationOutcomeBuilder withDetailsText(String details) {
      this.issue.getDetails().setText(details);
      return this.finalizer.apply(this.issue);
    }
  }
}
