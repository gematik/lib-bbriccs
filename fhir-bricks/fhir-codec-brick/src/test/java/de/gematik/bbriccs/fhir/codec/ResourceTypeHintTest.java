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

package de.gematik.bbriccs.fhir.codec;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import lombok.val;
import org.junit.jupiter.api.Test;

class ResourceTypeHintTest {

  @Test
  void shouldNotAllowRegistrationForAllVersionsIfNotEnum() {
    val typeHintBuilder = ResourceTypeHint.forStructure(new MyStructureDefinition());
    assertThrows(
        IllegalArgumentException.class,
        () -> typeHintBuilder.forAllVersionsFrom(MyCustomVersion.class));
  }

  private static class MyStructureDefinition implements WithStructureDefinition<MyCustomVersion> {
    @Override
    public String getCanonicalUrl() {
      return "https://example.com";
    }

    @Override
    public String getVersionedUrl(MyCustomVersion version) {
      return "https://example.com/" + version.getVersion();
    }
  }

  private static class MyCustomVersion implements ProfileVersion {
    @Override
    public String getVersion() {
      return "1.0.0";
    }

    @Override
    public String getName() {
      return "my.custom.version";
    }
  }
}
