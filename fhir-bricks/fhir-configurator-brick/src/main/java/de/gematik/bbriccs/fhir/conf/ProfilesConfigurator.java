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

package de.gematik.bbriccs.fhir.conf;

import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import de.gematik.bbriccs.toggle.FeatureToggle;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

@Slf4j
@Getter
public class ProfilesConfigurator {

  private static final String DEFAULT_SYS_PROP_TOGGLE = "bbriccs.fhir.profile";
  private static final String DEFAULT_RND_SYS_PROP_TOGGLE =
      String.valueOf(System.currentTimeMillis());
  private static final String DEFAULT_CONFIG_FILE_NAME = "fhir/configuration.yaml";

  private static final Map<String, ProfilesConfigurator> configCache = new HashMap<>();

  private final List<ProfileSettingsDto> profileConfigurations;
  private final String featureToggleName;
  private ProfileSettingsDto defaultProfile;

  private ProfilesConfigurator(
      List<ProfileSettingsDto> profileConfigurations, String featureToggleName) {
    this.profileConfigurations = profileConfigurations;
    this.featureToggleName = featureToggleName;

    // calculate the initial default profile
    // Note: the default profile can be changed by changing the system property at runtime
    this.defaultProfile = initializeDefaultProfile();
  }

  private ProfileSettingsDto initializeDefaultProfile() {
    val externalConfiguration = FeatureToggle.getStringToggle(featureToggleName);

    return externalConfiguration
        .map(
            cfg ->
                this.profileConfigurations.stream()
                    .filter(config -> config.getId().equalsIgnoreCase(cfg))
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new FhirConfigurationException(
                                format(
                                    "Configured Profile Setting {0} is not found within {1}",
                                    cfg,
                                    this.profileConfigurations.stream()
                                        .map(ProfileSettingsDto::getId)
                                        .collect(Collectors.joining(", "))))))
        .orElse(this.profileConfigurations.get(0));
  }

  public ProfileSettingsDto getDefaultProfile() {
    val externalConfiguration = FeatureToggle.getStringToggle(featureToggleName);
    externalConfiguration
        .filter(cfg -> !cfg.equalsIgnoreCase(defaultProfile.getId()))
        .ifPresent(cfg -> this.defaultProfile = initializeDefaultProfile());
    return defaultProfile;
  }

  public static ProfilesConfigurator getConfiguration(String name) {
    // use the random toggle which is guaranteed to never match a real feature toggle
    return getConfiguration(name, DEFAULT_RND_SYS_PROP_TOGGLE);
  }

  @SneakyThrows
  public static ProfilesConfigurator getConfiguration(String name, String featureToggleName) {
    val cfgFile =
        Optional.of(name)
            .map(rawName -> rawName.startsWith("fhir/") ? rawName : format("fhir/{0}", rawName))
            .map(
                rawName ->
                    FilenameUtils.isExtension(rawName, "yaml", "yml")
                        ? rawName
                        : format("{0}.yaml", rawName))
            .orElseThrow(); // NOSONAR will always contain a value here

    // calculate a key depending on filename and the name of the feature-toggle
    val configuratorKey = format("{0}-{1}", cfgFile, featureToggleName);
    return configCache.computeIfAbsent(
        configuratorKey, key -> createConfigurator(cfgFile, featureToggleName));
  }

  public static ProfilesConfigurator getDefaultConfiguration(String featureToggleName) {
    return getConfiguration(DEFAULT_CONFIG_FILE_NAME, featureToggleName);
  }

  public static ProfilesConfigurator getDefaultConfiguration() {
    return getConfiguration(DEFAULT_CONFIG_FILE_NAME, DEFAULT_SYS_PROP_TOGGLE);
  }

  /**
   * Search for a profile - including potential dependencies - in the virtual default profile
   * configuration by its name.
   *
   * @param profileName to search for
   * @return optional profile dto
   */
  public static Optional<ProfileDto> getVirtualDefaultProfile(String profileName) {
    // expand all profiles and potential dependencies of pre-populated snapshots
    return configCache.entrySet().stream()
        .flatMap(
            entry -> {
              val rootProfiles = entry.getValue().getDefaultProfile().getProfiles();
              val dependencies =
                  rootProfiles.stream()
                      .flatMap(
                          p ->
                              p.getDependsOn().stream()
                                  .flatMap(dep -> fromSnapshotName(dep).stream()));
              return Stream.concat(rootProfiles.stream(), dependencies);
            })
        .filter(profile -> profile.getName().equalsIgnoreCase(profileName))
        .findFirst();
  }

  /**
   * Try to extract profile information from a dependency snapshot name e.g.
   * de.gematik.epa.medication-1.3.0-snapshots.tgz will result in ProfileDto with name
   * "de.gematik.epa.medication" and version "1.3.0"
   *
   * @param name of the dependency snapshot
   * @return optional profile dto containing name and version
   */
  private static Optional<ProfileDto> fromSnapshotName(String name) {
    val nameParts = name.split("-", 0);
    // name should have at least two parts: my.profile.name-1.0.0-snapshot.tgz
    if (nameParts.length < 2) return Optional.empty();

    return VersionParser.parseVersion(nameParts[1]).stream()
        .map(
            v -> {
              val depDto = new ProfileDto();
              depDto.setName(nameParts[0]);
              depDto.setVersion(v);
              return depDto;
            })
        .findFirst();
  }

  private static void checkAndFillupProfileConfiguration(ProfileDto dto) {
    if (Strings.isNullOrEmpty(dto.getSnapshot())) {
      if (Strings.isNullOrEmpty(dto.getName()))
        throw new FhirConfigurationException(
            "Profile given without package or name! At least one is required");

      if (Strings.isNullOrEmpty(dto.getVersion()))
        throw new FhirConfigurationException(
            format("Profile version for {0} is required!", dto.getName()));
    }

    if (!Strings.isNullOrEmpty(dto.getName()) && !Strings.isNullOrEmpty(dto.getVersion())) {
      // package.tgz, name and version are given explicitly, not need to fill up anything
      return;
    }

    val finalVersion =
        Optional.ofNullable(dto.getVersion())
            .orElseGet(
                () ->
                    VersionParser.parseVersion(dto.getSnapshot())
                        .orElseThrow(
                            () ->
                                new FhirConfigurationException(
                                    format(
                                        "Unable to extract version from {0}", dto.getSnapshot()))));

    val finalName =
        Optional.ofNullable(dto.getName())
            .orElseGet(
                () -> {
                  // no explicit name was given, try to extract from package name
                  // 1. cut off possible -snapshot and -rc suffixes to proper calculation of the
                  // name
                  val pname = dto.getSnapshot().replace("-snap", "").replace("-rc", "");
                  // 2. reverse the string and find the first '-' to cut off the version and any
                  // suffixes
                  val reversedName = new StringBuilder(pname).reverse();
                  val cutIndex = reversedName.indexOf("-") + 1; // include the '-'
                  // 3. reverse back the remaining string to get the name without version
                  return reversedName.delete(0, cutIndex).reverse().toString();
                });

    dto.setName(finalName);
    dto.setVersion(finalVersion);
  }

  @SneakyThrows
  private static ProfilesConfigurator createConfigurator(String cfgFile, String featureToggleName) {
    val profilesConfig = ResourceLoader.readFileFromResource(cfgFile);
    val mapper =
        YAMLMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .build();
    val configuredProfiles =
        mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});

    configuredProfiles.stream()
        .flatMap(it -> it.getProfiles().stream())
        .forEach(ProfilesConfigurator::checkAndFillupProfileConfiguration);

    return new ProfilesConfigurator(configuredProfiles, featureToggleName);
  }
}
