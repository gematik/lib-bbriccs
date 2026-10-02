/*
 * Copyright (Change Date see Readme) gematik GmbH
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

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import de.gematik.bbriccs.fhir.conf.ProfileSettingsDto;
import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import de.gematik.bbriccs.fhir.exceptions.UnsupportedEncodingException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.LinkedList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

class ValidatorFhirFactoryTest {

  @Test
  void shouldThrowOnEmptyConfiguration() {
    val configuredProfiles = new LinkedList<ProfileSettingsDto>();
    assertThrows(
        FhirConfigurationException.class,
        () -> ValidatorFhirFactory.createValidator(configuredProfiles));
  }

  @Test
  void shouldThrowOnNullConfiguration() {
    assertThrows(
        FhirConfigurationException.class, () -> ValidatorFhirFactory.createValidator(null));
  }

  @Test
  void shouldThrowOnInvalidProfileFileExtensions() {
    val configuredProfiles = readCustomConfiguration("fhir/ihe-d_configuration_01.yaml");
    configuredProfiles.stream()
        .flatMap(psd -> psd.getProfiles().stream())
        .forEach(p -> p.setOmitProfiles(List.of("invalid.json")));
    val uee =
        assertThrows(
            UnsupportedEncodingException.class,
            () -> ValidatorFhirFactory.createValidator(configuredProfiles));
    assertTrue(uee.getMessage().contains("invalid.txt"));
  }

  @Test
  void shouldThrowOnInvalidProfileFile() {
    val configuredProfiles = readCustomConfiguration("fhir/ihe-d_configuration_01.yaml");
    configuredProfiles.stream()
        .flatMap(psd -> psd.getProfiles().stream())
        .forEach(p -> p.setOmitProfiles(List.of("invalid.txt")));
    val uee =
        assertThrows(
            FhirConfigurationException.class,
            () -> ValidatorFhirFactory.createValidator(configuredProfiles));
    assertTrue(uee.getMessage().contains("invalid.json"));
  }

  @Test
  void shouldNotThrowIfInvalidIsOmitted() {
    val configuredProfiles = readCustomConfiguration("fhir/ihe-d_configuration_02.yaml");
    assertDoesNotThrow(() -> ValidatorFhirFactory.createValidator(configuredProfiles));
  }

  @Test
  void shouldChooseSingleProfileValidator() {
    val configuredProfiles = readCustomConfiguration("fhir/single_profile_configuration.yaml");
    val validator = ValidatorFhirFactory.createValidator(configuredProfiles);
    assertEquals(ProfiledValidator.class, validator.getClass());
  }

  @Test
  void shouldThrowOnMissingProfileSnapshotFile() {
    val configuredProfiles = readCustomConfiguration("fhir/ihe-d_configuration_01.yaml");
    configuredProfiles.get(0).getProfiles().get(0).setSnapshot("my.profile-1.0.0-nonexistent.tgz");
    assertThrows(
        InternalErrorException.class,
        () -> ValidatorFhirFactory.createValidator(configuredProfiles));
  }

  @SneakyThrows
  private List<ProfileSettingsDto> readCustomConfiguration(String configFile) {
    val profilesConfig = ResourceLoader.readFileFromResource(configFile);
    val mapper =
        YAMLMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .build();
    return mapper.readValue(profilesConfig, new TypeReference<>() {});
  }
}
