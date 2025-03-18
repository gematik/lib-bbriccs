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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import de.gematik.bbriccs.utils.ResourceFileException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.SetSystemProperty;

class ProfilesConfiguratorTest {

  @Test
  void shouldNotInstantiateSingletonTwice() {
    val i1 = ProfilesConfigurator.getDefaultConfiguration();
    assertEquals(i1, ProfilesConfigurator.getDefaultConfiguration());
  }

  @Test
  void shouldReadDefaultConfig() {
    val config = ProfilesConfigurator.getDefaultConfiguration();
    assertEquals("1.2.0", config.getDefaultProfile().getId());
  }

  @ParameterizedTest
  @ValueSource(strings = {"fhir/configuration.yaml", "configuration.yaml", "configuration"})
  void shouldReadDefaultConfigurationByName(String name) {
    val config = ProfilesConfigurator.getConfiguration(name);
    assertEquals("1.2.0", config.getDefaultProfile().getId());
  }

  @Test
  void shouldThrowOnInvalidConfigurationName() {
    assertThrows(
        ResourceFileException.class,
        () -> ProfilesConfigurator.getConfiguration("fhir/epa-config.yml"));
  }

  @Test
  @SetSystemProperty(key = "bbriccs.fhir.profile.test1", value = "1.0.0")
  void shouldGetFromSystemProperty() {
    val config =
        ProfilesConfigurator.getConfiguration("configuration", "bbriccs.fhir.profile.test1");
    assertEquals("1.0.0", config.getDefaultProfile().getId());

    // toString the summary for code-coverage
    val profile = config.getDefaultProfile().getProfiles().get(0);
    val profileSummary = profile.toString();
    assertTrue(profileSummary.contains(profile.getName()));
    assertTrue(profileSummary.contains(profile.getVersion()));
  }

  @Test
  @SetSystemProperty(key = "bbriccs.fhir.profile.test2", value = "1.0.0.0")
  void shouldThrowOnInvalidSysPropertyConfiguration() {
    val exception =
        assertThrows(
            FhirConfigurationException.class,
            () ->
                ProfilesConfigurator.getConfiguration(
                    "configuration", "bbriccs.fhir.profile.test2"));
    assertTrue(exception.getMessage().contains("1.0.0.0 is not found"));
  }
}
