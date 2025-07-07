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

package de.gematik.bbriccs.fhir.de;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class DeBasisProfilVersionTest {

  @Test
  void shouldGetDefault() {
    // prepares the virtual default configuration
    val profiles = ProfilesConfigurator.getDefaultConfiguration();
    val dv = DeBasisProfilVersion.getDefaultVersion();
    assertEquals(DeBasisProfilVersion.V1_4_0, dv);
  }

  @Test
  void shouldGetVersionFromString() {
    val dv = DeBasisProfilVersion.fromString("0.9.13");
    assertEquals(DeBasisProfilVersion.V0_9_13, dv);
  }

  @Test
  @SetSystemProperty(key = "bbriccs.fhir.profile.de.basis.test", value = "1.3.2")
  void shouldGetDefaultVersionFromSysProp() {
    val toggleName = "bbriccs.fhir.profile.de.basis.test";
    // prepares the virtual default configuration
    val profiles = ProfilesConfigurator.getDefaultConfiguration(toggleName);
    val dv = DeBasisProfilVersion.getDefaultVersion();
    assertEquals(DeBasisProfilVersion.V1_3_2, dv);
  }
}
