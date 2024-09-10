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

package de.gematik.bbriccs.fhir.validation;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;

@Slf4j
public class MultiProfileValidator implements ValidatorFhir {

  private final FhirContext ctx;
  private final List<ProfiledValidator> profiledValidators;
  private final ProfiledValidator defaultProfileValidator;
  private final ProfileExtractor profileExtractor;

  private IParser xmlParser;
  private IParser jsonParser;

  public MultiProfileValidator(List<ProfiledValidator> profiledValidators) {
    this.ctx = FhirContext.forR4();
    this.profiledValidators = profiledValidators;
    this.defaultProfileValidator = profiledValidators.get(0);
    this.profileExtractor = new ProfileExtractor();
  }

  @Override
  public FhirContext getContext() {
    return this.ctx;
  }

  @Override
  public ValidationResult validate(String content) {
    if (this.profileExtractor.isUnprofiledSearchSet(content)) {
      return this.validateSearchsetBundle(content);
    } else {
      val p = chooseProfileValidator(() -> this.profileExtractor.extractProfile(content));
      return p.validate(content);
    }
  }

  private ValidationResult validateSearchsetBundle(String bundle) {
    val validationMessages = new LinkedList<SingleValidationMessage>();

    val parser =
        EncodingType.guessFromContent(bundle).choose(this::getXmlParser, this::getJsonParser);
    parser.parseResource(Bundle.class, bundle).getEntry().stream()
        .map(Bundle.BundleEntryComponent::getResource)
        .map(parser::encodeToString)
        .forEach(
            r -> {
              val vr = this.validate(r);
              validationMessages.addAll(vr.getMessages());
            });

    return new ValidationResult(this.getContext(), validationMessages);
  }

  private ProfiledValidator chooseProfileValidator(Supplier<Optional<String>> profileSupplier) {
    val profileUrlOpt = profileSupplier.get();
    AtomicReference<ProfiledValidator> chosenParser = new AtomicReference<>();
    profileUrlOpt.ifPresentOrElse(
        url -> chosenParser.set(chooseProfileValidator(url)),
        () -> {
          val defaultParser = this.defaultProfileValidator;
          log.warn(
              format(
                  "Could not determine the Profile from given content! Use default Validator"
                      + " ''{0}''",
                  defaultParser.getId()));
          chosenParser.set(defaultParser);
        });

    val chosenValidator = chosenParser.get();
    val profileUrl = profileUrlOpt.orElse("no found profile");
    log.trace(format("Choose Validator {0} for {1}", chosenValidator.getId(), profileUrl));
    return chosenValidator;
  }

  private ProfiledValidator chooseProfileValidator(String profileUrl) {
    val validator =
        this.profiledValidators.stream().filter(p -> p.doesSupport(profileUrl)).findFirst();

    if (validator.isPresent()) {
      log.trace(
          format(
              "Use Validator Configuration ''{0}'' for {1}", validator.get().getId(), profileUrl));
    } else {
      log.warn(
          format(
              "No supporting Validator found for {0}, use Validator Configuration ''{1}'' as"
                  + " default",
              profileUrl, this.defaultProfileValidator.getId()));
    }

    return validator.orElse(this.defaultProfileValidator);
  }

  private IParser getXmlParser() {
    if (this.xmlParser == null) {
      this.xmlParser = this.getContext().newXmlParser();
    }
    return this.xmlParser;
  }

  public IParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = this.getContext().newJsonParser();
    }
    return this.jsonParser;
  }
}
