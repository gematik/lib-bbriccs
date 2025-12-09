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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.refv.commons.validation.ProfileValidityPeriodCheckStrategy;
import de.gematik.refv.commons.validation.ValidationModule;
import de.gematik.refv.commons.validation.ValidationOptions;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

@Slf4j
public class ReferenzValidator extends ValidatorFhirBase {

  private final ValidationModule validationModule;
  private final FhirContext context; // required to be able to map back refVR to HAPIs VR
  private final ValidationOptions options;

  private final ValidatorFhir unprofiledValidator;

  private IParser xmlParser;
  private IParser jsonParser;

  public ReferenzValidator(
      FhirContext ctx, ValidationModule validationModule, ValidationOptions options) {
    this.context = ctx;
    this.validationModule = validationModule;
    this.options = options;
    this.unprofiledValidator = new NonProfiledValidator(this.context);
  }

  @Override
  public FhirContext getContext() {
    return this.context;
  }

  @Override
  public ValidationResult validate(String content) {
    val nullSafeContent = Objects.requireNonNullElse(content, "");

    if (this.profileExtractor.isUnprofiledSearchSet(content)) {
      log.warn(
          "Detected unprofiled Bundle of type SEARCHSET or COLLECTION - validating entries"
              + " separately");
      val parser =
          EncodingType.guessFromContent(nullSafeContent)
              .choose(this::getXmlParser, this::getJsonParser);
      val bundleResource = parser.parseResource(Bundle.class, nullSafeContent);
      return validateUnprofiledBundle(bundleResource);
    } else if (hasProfile(content)) {
      log.info("Validating profiled resource with ReferenzValidator");
      return validateWithRefVal(content);
    } else {
      val msg =
          format(
              "use generic unprofiled validator for resource: {0}",
              profileExtractor.shortenContentForLogging(content));
      log.info(msg);
      return this.unprofiledValidator.validate(content);
    }
  }

  @Override
  public ValidationResult validate(IBaseResource resource) {
    if (this.isCollectionBundle(resource)) {
      return this.validateUnprofiledBundle((Bundle) resource);
    } else if (this.hasProfile(resource)) {
      // resource has a profile so RefVal can handle it
      val content = getXmlParser().encodeResourceToString(resource);
      return this.validate(content);
    } else {
      val msg =
          format(
              "use generic unprofiled validator for resource {0}",
              resource.getClass().getSimpleName());
      log.info(msg);
      return this.unprofiledValidator.validate(resource);
    }
  }

  /**
   * check if the content contains any profiles Note: RefVal does not know
   * http://hl7.org/fhir/StructureDefinition/OperationOutcome thus skip as well
   *
   * @param content
   * @return true if content has a profile other than OperationOutcome
   */
  private boolean hasProfile(String content) {
    return this.profileExtractor.extractProfile(content).stream()
        .anyMatch(p -> !p.contains("OperationOutcome"));
  }

  private ValidationResult validateWithRefVal(String content) {
    try {
      val refVr = this.validationModule.validateString(content, options);
      return new ValidationResult(
          this.getContext(), refVr.getValidationMessages().stream().toList());
    } catch (Exception e) {
      /*
      some sort of error led to an Exception: handle this case via ValidationResult=ERROR
       */
      log.error("Error while validating FHIR content", e);
      val svm = ValidationMessageUtil.createErrorMessage(e.getMessage());
      return new ValidationResult(this.getContext(), List.of(svm));
    }
  }

  private IParser getXmlParser() {
    if (this.xmlParser == null) {
      this.xmlParser =
          this.getContext().newXmlParser().setOverrideResourceIdWithBundleEntryFullUrl(false);
    }
    return this.xmlParser;
  }

  private IParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser =
          this.getContext().newJsonParser().setOverrideResourceIdWithBundleEntryFullUrl(false);
    }
    return this.jsonParser;
  }

  /**
   * Bundles which do not have a profile are usually a searchset or collection and thus might
   * contain entry resources coming from different profiles. Validating these directly with a single
   * validator will result errors because of validating against wrong profiles.
   *
   * <p>Therefore the entry resources of such a bundle must be validated separately, each with it's
   * own choice of a profileset to validate against
   *
   * @param bundle to be validated
   * @return a {@link ValidationResult} for the entry resources of the bundle
   */
  private ValidationResult validateUnprofiledBundle(Bundle bundle) {
    val validationMessages = new LinkedList<SingleValidationMessage>();

    bundle.getEntry().stream()
        .map(Bundle.BundleEntryComponent::getResource)
        .forEach(
            r -> {
              val vr =
                  this.validate(
                      r); // attention might lead to recursion if unprofiled bundles contain
              // unprofiled bundles
              validationMessages.addAll(vr.getMessages());
            });

    return new ValidationResult(this.getContext(), validationMessages);
  }

  public static ValidatorFhir withValidationModule(SupportedValidationModule svm) {
    return withValidationModule(FhirContext.forR4(), svm);
  }

  @SneakyThrows
  public static ValidatorFhir withValidationModule(FhirContext ctx, SupportedValidationModule svm) {
    return withValidationModule(ctx, new ValidationModuleFactory().createValidationModule(svm));
  }

  public static ValidatorFhir withValidationModule(
      FhirContext ctx, ValidationModule validationModule) {

    val options = ValidationOptions.getDefaults();
    options.setProfileValidityPeriodCheckStrategy(ProfileValidityPeriodCheckStrategy.IGNORE);
    options.setAcceptedEncodings(List.of("xml", "json"));

    return withValidationModule(ctx, validationModule, options);
  }

  public static ValidatorFhir withValidationModule(
      FhirContext ctx, ValidationModule validationModule, ValidationOptions options) {
    return new ReferenzValidator(ctx, validationModule, options);
  }
}
