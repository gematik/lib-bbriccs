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

package de.gematik.bbriccs.fhir.conf;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import de.gematik.bbriccs.toggle.FeatureToggle;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;

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

  public static Optional<ProfileDto> getVirtualDefaultProfile(String profileName) {
    return configCache.entrySet().stream()
        .flatMap(entry -> entry.getValue().getDefaultProfile().getProfiles().stream())
        .filter(profile -> profile.getName().equalsIgnoreCase(profileName))
        .findFirst();
  }

  @SneakyThrows
  private static ProfilesConfigurator createConfigurator(String cfgFile, String featureToggleName) {
    val profilesConfig = ResourceLoader.readFileFromResource(cfgFile);
    val mapper =
        new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    val configuredProfiles =
        mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});

    return new ProfilesConfigurator(configuredProfiles, featureToggleName);
  }
}
