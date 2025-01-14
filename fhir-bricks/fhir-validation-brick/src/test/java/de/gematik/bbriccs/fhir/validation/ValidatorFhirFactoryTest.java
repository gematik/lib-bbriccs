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

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.bbriccs.fhir.conf.ProfileSettingsDto;
import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import de.gematik.bbriccs.fhir.exceptions.UnsupportedEncodingException;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ValidatorFhirFactoryTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ValidatorFhirFactory.class));
  }

  @Test
  void shouldThrowOnEmptyConfiguration() {
    val configuredProfiles = new LinkedList<ProfileSettingsDto>();
    val ctx = FhirContext.forR4();
    assertThrows(
        FhirConfigurationException.class,
        () -> ValidatorFhirFactory.createValidator(ctx, configuredProfiles));
  }

  @Test
  void shouldThrowOnNullConfiguration() {
    val ctx = FhirContext.forR4();
    assertThrows(
        FhirConfigurationException.class, () -> ValidatorFhirFactory.createValidator(ctx, null));
  }

  @Test
  void shouldThrowOnInvalidProfileFileExtensions() throws JsonProcessingException {
    val profilesConfig = ResourceLoader.readFileFromResource("fhir/ihe-d_configuration_01.yaml");
    val mapper =
        new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    val configuredProfiles =
        mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});
    configuredProfiles.stream()
        .flatMap(psd -> psd.getProfiles().stream())
        .forEach(p -> p.setOmitProfiles(List.of("invalid.json")));
    val ctx = FhirContext.forR4();
    val uee =
        assertThrows(
            UnsupportedEncodingException.class,
            () -> ValidatorFhirFactory.createValidator(ctx, configuredProfiles));
    assertTrue(uee.getMessage().contains("invalid.txt"));
  }

  @Test
  void shouldThrowOnInvalidProfileFile() throws JsonProcessingException {
    val profilesConfig = ResourceLoader.readFileFromResource("fhir/ihe-d_configuration_01.yaml");
    val mapper =
        new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    val configuredProfiles =
        mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});
    configuredProfiles.stream()
        .flatMap(psd -> psd.getProfiles().stream())
        .forEach(p -> p.setOmitProfiles(List.of("invalid.txt")));
    val ctx = FhirContext.forR4();
    val uee =
        assertThrows(
            FhirConfigurationException.class,
            () -> ValidatorFhirFactory.createValidator(ctx, configuredProfiles));
    assertTrue(uee.getMessage().contains("invalid.json"));
  }

  @Test
  void shouldNotThrowIfInvalidIsOmitted() throws JsonProcessingException {
    val profilesConfig = ResourceLoader.readFileFromResource("fhir/ihe-d_configuration_02.yaml");
    val mapper =
        new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    val configuredProfiles =
        mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});
    val ctx = FhirContext.forR4();
    assertDoesNotThrow(() -> ValidatorFhirFactory.createValidator(ctx, configuredProfiles));
  }

  @Test
  void shouldChooseSingleProfileValidator() throws JsonProcessingException {
    val profilesConfig =
        ResourceLoader.readFileFromResource("fhir/single_profile_configuration.yaml");
    val mapper =
        new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    val configuredProfiles =
        mapper.readValue(profilesConfig, new TypeReference<List<ProfileSettingsDto>>() {});
    val validator = ValidatorFhirFactory.createValidator(FhirContext.forR4(), configuredProfiles);
    assertEquals(ProfiledValidator.class, validator.getClass());
  }
}
