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

package de.gematik.bbriccs.toggle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class FeatureConfigurationTest {

  @Test
  void shouldNotHaveUndefinedToggles() {
    assertFalse(FeatureToggle.hasToggle("hello.world"));
    assertEquals("Default Value", FeatureToggle.getStringToggle("hello.world", "Default Value"));
  }

  @Test
  @SetSystemProperty(key = "hello.world", value = "Toggle Config")
  void shouldHaveSimpleToggle() {
    assertTrue(FeatureToggle.hasToggle("hello.world"));
    assertEquals("Toggle Config", FeatureToggle.getStringToggle("hello.world", "Default Value"));
  }

  @Test
  @SetSystemProperty(key = "hello.boolean", value = "Yes")
  void shouldHaveCustomBooleanToggleValue() {
    val fc = new FeatureConfiguration();
    assertTrue(fc.getBooleanToggle("hello.boolean"));
  }

  @Test
  @SetSystemProperty(key = "hello.boolean", value = "TRUE")
  void shouldHaveBooleanToggle() {
    val fc = new FeatureConfiguration();
    assertTrue(fc.getBooleanToggle("hello.boolean"));
  }

  @Test
  @SetSystemProperty(key = "hello.boolean", value = "False")
  void shouldHaveDefaultBooleanToggle() {
    assertTrue(FeatureToggle.getBooleanToggle("hello.boolean2", true));
  }

  @Test
  void shouldHaveDefaultBooleanToggleValue() {
    val fc = new FeatureConfiguration();
    assertFalse(fc.hasToggle("hello.boolean"));
    assertFalse(fc.getBooleanToggle("hello.boolean2"));
  }

  @Test
  @SetSystemProperty(key = "hello.integer", value = "100")
  void shouldHaveIntegerToggle() {
    val fc = new FeatureConfiguration();
    assertEquals(100, fc.getIntegerToggle("hello.integer"));
  }

  @Test
  void shouldHaveDefaultIntegerToggles() {
    val fc = new FeatureConfiguration();
    assertEquals(0, fc.getIntegerToggle("hello.integer"));
    assertEquals(10, FeatureToggle.getIntegerToggle("hello.integer", 10));
  }

  @Test
  @SetSystemProperty(key = "hello.double", value = "200.5")
  void shouldHaveDoubleToggle() {
    val fc = new FeatureConfiguration();
    assertEquals(200.5, fc.getDoubleToggle("hello.double"));
  }

  @Test
  void shouldHaveDefaultDoubleToggles() {
    val fc = new FeatureConfiguration();
    assertEquals(0, fc.getDoubleToggle("hello.double"));
    assertEquals(10.1, fc.getDoubleToggle("hello.double", 10.1));
  }

  @Test
  @SetSystemProperty(key = "hello.enum", value = "hello_world")
  void shouldMapToEnum() {
    val fc = new FeatureConfiguration();
    assertEquals(
        TestToggle.HELLO_WORLD,
        fc.getEnumToggle("hello.enum", TestToggle.class, TestToggle.TOGGLE_A));
  }

  @Test
  void shouldMapToDefaultEnum() {
    val fc = new FeatureConfiguration();
    assertFalse(fc.hasToggle("hello.enum"));
    assertEquals(
        TestToggle.TOGGLE_A, fc.getEnumToggle("hello.enum", TestToggle.class, TestToggle.TOGGLE_A));
  }

  @Test
  @SetSystemProperty(key = "hello.enum", value = "hello_world")
  void shouldMapFromFeatureToggle() {
    val fc = new FeatureConfiguration();
    val tft = new TestFeatureToggle();

    assertTrue(fc.hasToggle("hello.enum"));
    assertEquals(TestToggle.HELLO_WORLD, fc.getToggle(tft));
  }

  @Test
  void shouldMapFromDefaultFeatureToggle() {
    val fc = new FeatureConfiguration();
    val tft = new TestFeatureToggle();

    assertFalse(fc.hasToggle("hello.enum"));
    assertEquals(TestToggle.TOGGLE_B, fc.getToggle(tft));
  }

  public enum TestToggle {
    TOGGLE_A,
    TOGGLE_B,
    HELLO_WORLD
  }

  public static class TestFeatureToggle implements FeatureToggle<TestToggle> {

    @Override
    public String getKey() {
      return "hello.enum";
    }

    @Override
    public Function<String, TestToggle> getConverter() {
      return value -> TestToggle.valueOf(value.toUpperCase());
    }

    @Override
    public TestToggle getDefaultValue() {
      return TestToggle.TOGGLE_B;
    }
  }
}
