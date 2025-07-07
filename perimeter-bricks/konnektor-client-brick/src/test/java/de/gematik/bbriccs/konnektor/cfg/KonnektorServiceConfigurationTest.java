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

package de.gematik.bbriccs.konnektor.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.konnektor.KonnektorBuildInstruction;
import de.gematik.bbriccs.konnektor.KonnektorFactory;
import de.gematik.bbriccs.konnektor.exceptions.InvalidKonnektorServiceConfigurationException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.junit.jupiter.api.Test;

class KonnektorServiceConfigurationTest {

  @Test
  void shouldCastToConcreteType() {
    KonnektorServiceConfiguration cfg = new ConcreteKonnektorServiceConfiguration();
    cfg.setType("Test Configuration");
    val serviceFactory = new ConcreteKonnektorFactory();
    val castedCfg =
        assertDoesNotThrow(
            () -> cfg.castTo(serviceFactory, ConcreteKonnektorServiceConfiguration.class));
    assertEquals("Test Configuration", castedCfg.getType());
    assertEquals("test field", castedCfg.getTestField());
  }

  @Test
  void shouldThrowOnIncompatibleConcreteType() {
    KonnektorServiceConfiguration cfg = new ConcreteKonnektorServiceConfiguration2();
    cfg.setType("Test Configuration");
    val serviceFactory = new ConcreteKonnektorFactory();
    val exception =
        assertThrows(
            InvalidKonnektorServiceConfigurationException.class,
            () -> cfg.castTo(serviceFactory, ConcreteKonnektorServiceConfiguration.class));
    assertTrue(exception.getMessage().contains(serviceFactory.getType()));
    assertTrue(exception.getMessage().contains(cfg.getType()));
    assertTrue(
        exception
            .getMessage()
            .contains(ConcreteKonnektorServiceConfiguration2.class.getSimpleName()));
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  private static class ConcreteKonnektorServiceConfiguration extends KonnektorServiceConfiguration {
    private String testField = "test field";
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  private static class ConcreteKonnektorServiceConfiguration2
      extends KonnektorServiceConfiguration {
    private String testField2 = "test field2";
  }

  private static class ConcreteKonnektorFactory implements KonnektorFactory {

    @Override
    public String getType() {
      return "Test Factory";
    }

    @Override
    public KonnektorBuildInstruction mapConfiguration(KonnektorConfiguration cfg) {
      return null;
    }
  }
}
