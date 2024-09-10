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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.conf.exceptions.InvalidConfigurationException;
import de.gematik.bbriccs.utils.SingletonUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class ProfilesConfiguratorTest {

  @BeforeEach
  void cleanupSingleton() {
    SingletonUtil.resetSingleton(ProfilesConfigurator.class);
  }

  @Test
  void shouldNotInstantiateSingletonTwice() {
    val i1 = ProfilesConfigurator.getInstance();
    assertEquals(i1, ProfilesConfigurator.getInstance());
  }

  @Test
  void shouldReadConfig() {
    val config = ProfilesConfigurator.getInstance();
    assertEquals("1.2.0", config.getDefaultProfile().getId());
  }

  @Test
  @SetSystemProperty(key = "bbriccs.fhir.profile", value = "1.0.0")
  void shouldGetFromSystemProperty() {
    val config = ProfilesConfigurator.getInstance();
    assertEquals("1.0.0", config.getDefaultProfile().getId());
  }

  @Test
  @SetSystemProperty(key = "bbriccs.fhir.profile", value = "1.0.0")
  void shouldToStringSummary() {
    val config = ProfilesConfigurator.getInstance();
    assertEquals("1.0.0", config.getDefaultProfile().getId());

    val profile = config.getDefaultProfile().getProfiles().get(0);
    val profileSummary = profile.toString();
    assertTrue(profileSummary.contains(profile.getName()));
    assertTrue(profileSummary.contains(profile.getVersion()));
  }

  @Test
  @SetSystemProperty(key = "bbriccs.fhir.profile", value = "1.0.0.0")
  void shouldThrowOnInvalidSysPropertyConfiguration() {
    val exception =
        assertThrows(InvalidConfigurationException.class, ProfilesConfigurator::getInstance);
    assertTrue(exception.getMessage().contains("1.0.0.0 is not found"));
  }
}
