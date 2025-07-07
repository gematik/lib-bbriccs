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
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.IParser;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.conf.ProfileDto;
import de.gematik.bbriccs.fhir.conf.ProfileSettingsDto;
import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import de.gematik.bbriccs.fhir.validation.support.CodeSystemFilter;
import de.gematik.bbriccs.fhir.validation.support.ErrorMessageFilter;
import de.gematik.bbriccs.fhir.validation.support.ProfileValidationSupport;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

@Slf4j
public class ValidatorFhirFactory {

  static {
    /* this will force HAPI to produce error messages in english; by that we can filter messages reliably */
    Locale.setDefault(new Locale("en", "DE"));
  }

  private ValidatorFhirFactory() {
    throw new IllegalAccessError("utility class");
  }

  public static ValidatorFhir createValidator() {
    return createValidator(FhirContext.forR4());
  }

  public static ValidatorFhir createValidator(FhirContext ctx) {
    val profileSettings = ProfilesConfigurator.getDefaultConfiguration();
    return createValidator(ctx, profileSettings.getProfileConfigurations());
  }

  public static ValidatorFhir createValidator(
      FhirContext ctx, List<ProfileSettingsDto> profileSettings) {
    if (profileSettings == null || profileSettings.isEmpty()) {
      throw new FhirConfigurationException(
          "FHIR Configuration does not contain any profile settings");
    } else if (profileSettings.size() == 1) {
      return createSingleProfileValidator(ctx, profileSettings.get(0));
    }

    val validators =
        profileSettings.stream().map(ps -> createSingleProfileValidator(ctx, ps)).toList();
    return new MultiProfileValidator(validators);
  }

  private static ProfiledValidator createSingleProfileValidator(
      FhirContext ctx, ProfileSettingsDto profileSettings) {
    val supports = create(ctx, profileSettings);
    val errorFilter = new ErrorMessageFilter(profileSettings.getErrorFilter());
    return new ProfiledValidator(ctx, profileSettings.getId(), supports, errorFilter);
  }

  private static List<IValidationSupport> create(
      FhirContext ctx, ProfileSettingsDto profileSettings) {

    val supports = new ArrayList<IValidationSupport>(profileSettings.getProfiles().size() + 1);

    Optional.ofNullable(profileSettings.getIgnoreCodeSystems())
        .filter(ignoredCodeSystems -> !ignoredCodeSystems.isEmpty())
        .ifPresent(
            ignoredCodeSystems -> supports.add(new CodeSystemFilter(ctx, ignoredCodeSystems)));

    val profileSupports =
        profileSettings.getProfiles().stream().map(profile -> create(ctx, profile)).toList();
    supports.addAll(profileSupports);
    return supports;
  }

  private static IValidationSupport create(FhirContext ctx, ProfileDto profile) {
    return new Builder(ctx, profile).build();
  }

  private static class Builder {

    private final Map<String, StructureDefinition> structureDefinitions = new HashMap<>();
    private final Map<String, NamingSystem> namingSystems = new HashMap<>();
    private final Map<String, CodeSystem> codeSystems = new HashMap<>();
    private final Map<String, ValueSet> valueSets = new HashMap<>();

    private final FhirContext ctx;
    private final IParser jsonParser;
    private final IParser xmlParser;
    private final ProfileDto profile;

    private Builder(FhirContext ctx, ProfileDto profile) {
      this.ctx = ctx;
      this.jsonParser = ctx.newJsonParser();
      this.xmlParser = ctx.newXmlParser();
      this.profile = profile;
    }

    @SneakyThrows
    private ProfileValidationSupport build() {
      // Note: fhir/profiles is a convention for the resource path of FHIR profiles
      val packageName =
          format("fhir/profiles/{0}-{1}/package", profile.getName(), profile.getVersion());

      ClassPath.from(ValidatorFhirFactory.class.getClassLoader()).getResources().stream()
          .filter(info -> info.getResourceName().startsWith(packageName))
          .filter(info -> !info.getResourceName().contains("package.json"))
          .filter(
              info ->
                  profile.getOmitProfiles().stream().noneMatch(info.getResourceName()::contains))
          .forEach(this::initProfile);

      return new ProfileValidationSupport(
          ctx, profile, structureDefinitions, namingSystems, codeSystems, valueSets);
    }

    private void initProfile(ResourceInfo info) {
      val profileContent = ResourceLoader.readFileFromResource(info.getResourceName());
      val fileSizeMb = profileContent.length() / (1024 * 1024);

      if (fileSizeMb > 1) {
        log.warn("Large Profile ({} MB) detected - {}", fileSizeMb, info.getResourceName());
        log.warn(
            "\tThis might lead to excessive memory consumption: make sure you really need {} and"
                + " consider the ''omitProfiles'' option",
            info.getResourceName());
      } else {
        log.trace("Load Profile ({} MB) - {}", fileSizeMb, info.getResourceName());
      }

      val parser =
          EncodingType.chooseAppropriateParser(
              info.getResourceName(), this.xmlParser, this.jsonParser);

      try {
        val resource = parser.parseResource(profileContent);
        this.addResource(resource);
      } catch (Exception e) {
        val message =
            format("Something went wrong while reading profile {0}", info.getResourceName());
        throw new FhirConfigurationException(message, e);
      }
    }

    private <T extends IBaseResource> void addResource(T resource) {
      val typeName = resource.fhirType();
      boolean added = true;
      if (resource instanceof StructureDefinition structureDefinition) {
        val key = structureDefinition.getUrl();
        this.structureDefinitions.put(key, structureDefinition);
      } else if (resource instanceof NamingSystem namingSystem) {
        val key = namingSystem.getUniqueId().get(0).getValue();
        this.namingSystems.put(key, namingSystem);
      } else if (resource instanceof CodeSystem codeSystem) {
        val key = codeSystem.getUrl();
        this.codeSystems.put(key, codeSystem);
      } else if (resource instanceof ValueSet valueSet) {
        val key = valueSet.getUrl();
        this.valueSets.put(key, valueSet);
      } else {
        added = false;
        log.trace(
            "\tProfile-Resource of type {} ({}) is omitted",
            typeName,
            resource.getClass().getSimpleName());
      }

      if (added) {
        log.trace("\tPut {} to profile {}", typeName, this.profile);
      }
    }
  }
}
