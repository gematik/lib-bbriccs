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

package de.gematik.bbriccs.fhir.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.util.Strings;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;

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
    val nullSafeContent = Objects.requireNonNullElse(content, "");
    if (this.profileExtractor.isUnprofiledSearchSet(nullSafeContent)) {
      val parser =
          EncodingType.guessFromContent(nullSafeContent)
              .choose(this::getXmlParser, this::getJsonParser);
      val bundleResource = parser.parseResource(Bundle.class, nullSafeContent);
      return validateUnprofiledBundle(bundleResource);
    } else {
      val p = chooseProfileValidator(() -> this.profileExtractor.extractProfile(nullSafeContent));
      return p.validate(nullSafeContent);
    }
  }

  @Override
  public ValidationResult validate(IBaseResource resource) {
    // instead of using the default implementation, we can use the resource directly for easier
    // decision if this is an unprofiled bundle
    val isUnprofiled =
        resource.getMeta().getProfile().stream()
            .map(IPrimitiveType::getValue)
            .filter(p -> !Strings.isNullOrEmpty(p))
            .toList()
            .isEmpty();
    val isCollectionBundle =
        Optional.of(resource)
            .filter(Bundle.class::isInstance)
            .map(r -> (Bundle) r)
            .filter(
                bundle ->
                    bundle.getType() == BundleType.COLLECTION
                        || bundle.getType() == BundleType.SEARCHSET)
            .isPresent();

    if (isUnprofiled && isCollectionBundle) {
      return validateUnprofiledBundle((Bundle) resource);
    } else {
      val parser =
          this.getXmlParser()
              .setOverrideResourceIdWithBundleEntryFullUrl(false)
              .setOmitResourceId(false);
      val content = parser.encodeResourceToString(resource);
      return this.validate(content);
    }
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
              val validator = chooseProfileValidator(r);
              val vr = validator.validate(r);
              validationMessages.addAll(vr.getMessages());
            });

    return new ValidationResult(this.getContext(), validationMessages);
  }

  private ProfiledValidator chooseProfileValidator(IBaseResource resource) {
    val profile =
        resource.getMeta().getProfile().stream().map(IPrimitiveType::getValue).findFirst();
    return chooseProfileValidator(() -> profile);
  }

  private ProfiledValidator chooseProfileValidator(Supplier<Optional<String>> profileSupplier) {
    val profileUrlOpt = profileSupplier.get();
    AtomicReference<ProfiledValidator> chosenParser = new AtomicReference<>();
    profileUrlOpt.ifPresentOrElse(
        url -> chosenParser.set(chooseProfileValidator(url)),
        () -> {
          val defaultParser = this.defaultProfileValidator;
          log.warn(
              "Could not determine the Profile from given content! Use default Validator '{}' as"
                  + " fallback",
              defaultParser.getId());
          chosenParser.set(defaultParser);
        });

    val chosenValidator = chosenParser.get();
    val profileUrl = profileUrlOpt.orElse("unknown profile");
    log.trace("Choose Validator {} for {}", chosenValidator.getId(), profileUrl);
    return chosenValidator;
  }

  private ProfiledValidator chooseProfileValidator(String profileUrl) {
    val validator =
        this.profiledValidators.stream().filter(p -> p.doesSupport(profileUrl)).findFirst();

    if (validator.isPresent()) {
      log.trace("Use Validator Configuration ''{}'' for {}", validator.get().getId(), profileUrl);
    } else {
      log.warn(
          "No supporting Validator found for {}, use Validator Configuration '{}' as default",
          profileUrl,
          this.defaultProfileValidator.getId());
    }

    return validator.orElse(this.defaultProfileValidator);
  }

  private IParser getXmlParser() {
    if (this.xmlParser == null) {
      this.xmlParser =
          this.getContext().newXmlParser().setOverrideResourceIdWithBundleEntryFullUrl(false);
    }
    return this.xmlParser;
  }

  public IParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser =
          this.getContext().newJsonParser().setOverrideResourceIdWithBundleEntryFullUrl(false);
    }
    return this.jsonParser;
  }
}
