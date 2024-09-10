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
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.IParser;
import de.gematik.bbriccs.fhir.conf.ProfileDto;
import de.gematik.bbriccs.fhir.conf.ProfileSettingsDto;
import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.fhir.conf.exceptions.InvalidConfigurationException;
import de.gematik.bbriccs.fhir.validation.support.ErrorMessageFilter;
import de.gematik.bbriccs.fhir.validation.support.ProfileValidationSupport;
import de.gematik.bbriccs.fhir.validation.support.ValueSetFilter;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
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
    val profileSettings = ProfilesConfigurator.getInstance();
    return createValidator(ctx, profileSettings.getProfileConfigurations());
  }

  public static ValidatorFhir createValidator(
      FhirContext ctx, List<ProfileSettingsDto> profileSettings) {
    if (profileSettings == null || profileSettings.isEmpty()) {
      throw new InvalidConfigurationException(
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

    Optional.ofNullable(profileSettings.getOmitValueSets())
        .filter(omitValues -> !omitValues.isEmpty())
        .ifPresent(omitValues -> supports.add(new ValueSetFilter(ctx, omitValues)));

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
    private final IParser parser;
    private final ProfileDto profile;

    private Builder(FhirContext ctx, ProfileDto profile) {
      this.ctx = ctx;
      this.parser = ctx.newJsonParser();
      this.profile = profile;
    }

    @SneakyThrows
    private void initProfile(File profileFile) {
      val fileSizeMb = profileFile.length() / (1024 * 1024);
      if (fileSizeMb > 1) {
        log.warn("Large Profile ({} MB) detected - {}", fileSizeMb, profileFile.getCanonicalPath());
        log.warn(
            "\tThis might lead to excessive memory consumption: make sure you really need {} and"
                + " consider the ''omitProfiles'' option",
            profileFile.getName());
      } else {
        log.trace("Load Profile ({} MB) - {}", fileSizeMb, profileFile.getName());
      }

      val input = ResourceLoader.readString(profileFile);
      val resource = this.parser.parseResource(input);
      this.addResource(resource);
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
            format(
                "\tProfile-Resource of type {0} ({1}) is omitted",
                typeName, resource.getClass().getSimpleName()));
      }

      if (added) {
        log.trace(format("\tPut {0} to profile {1}", typeName, this.profile));
      }
    }

    private ProfileValidationSupport build() {
      ResourceLoader.getResourceFilesInDirectory(
              format("fhir/profiles/{0}-{1}/package", profile.getName(), profile.getVersion()))
          .stream()
          .filter(f -> !f.getName().equals("package.json"))
          .filter(f -> !profile.getOmitProfiles().contains(f.getName()))
          .forEach(this::initProfile);

      return new ProfileValidationSupport(
          ctx, profile, structureDefinitions, namingSystems, codeSystems, valueSets);
    }
  }
}
