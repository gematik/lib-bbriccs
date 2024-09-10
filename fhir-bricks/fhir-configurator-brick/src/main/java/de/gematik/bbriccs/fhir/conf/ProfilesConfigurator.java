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

package de.gematik.bbriccs.fhir.conf;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.bbriccs.fhir.conf.exceptions.InvalidConfigurationException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

@Getter
public class ProfilesConfigurator {

  private static final String ENV_TOGGLE = "BBRICCS_FHIR_PROFILE";
  private static final String SYS_PROP_TOGGLE = "bbriccs.fhir.profile";
  private static final String CONFIG_FILE_NAME = "fhir/configuration.yaml";
  private static ProfilesConfigurator instance;
  private final List<ProfileSettingsDto> profileConfigurations;
  private final ProfileSettingsDto defaultProfile;

  private ProfilesConfigurator(
      List<ProfileSettingsDto> profileConfigurations, ProfileSettingsDto defaultProfile) {
    this.profileConfigurations = profileConfigurations;
    this.defaultProfile = defaultProfile;
  }

  @SneakyThrows
  public static ProfilesConfigurator getInstance() {
    if (instance == null) {

      val profilesConfig = ResourceLoader.readFileFromResource(CONFIG_FILE_NAME);

      val mapper =
          new ObjectMapper(new YAMLFactory())
              .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
      val configuredProfiles =
          mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});
      val externalConfiguration = System.getProperty(SYS_PROP_TOGGLE, System.getenv(ENV_TOGGLE));

      ProfileSettingsDto defaultConfig;
      if (externalConfiguration != null) {
        defaultConfig =
            configuredProfiles.stream()
                .filter(config -> config.getId().equalsIgnoreCase(externalConfiguration))
                .findFirst()
                .orElseThrow(
                    () ->
                        new InvalidConfigurationException(
                            format(
                                "Configured Profile Setting {0} is not found within {1}",
                                externalConfiguration,
                                configuredProfiles.stream()
                                    .map(ProfileSettingsDto::getId)
                                    .collect(Collectors.joining(", ")))));
      } else {
        defaultConfig = configuredProfiles.get(0);
      }
      instance = new ProfilesConfigurator(configuredProfiles, defaultConfig);
    }

    return instance;
  }
}
