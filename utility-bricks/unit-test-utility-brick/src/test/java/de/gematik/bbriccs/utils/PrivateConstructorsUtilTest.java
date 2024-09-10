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

package de.gematik.bbriccs.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class PrivateConstructorsUtilTest {

  @Test
  void shouldNotHaveCallableConstructor() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(PrivateConstructorsUtil.class));
  }

  @Test
  void shouldDetectPublicConstructors() {
    assertFalse(PrivateConstructorsUtil.isUtilityConstructor(TestClassPublicConstructor.class));
  }

  @Test
  void shouldDetectPublicConstructors02() {
    assertFalse(
        PrivateConstructorsUtil.throwsOnCall(
            InvocationTargetException.class, TestClassPublicConstructor.class));
  }

  @Test
  void shouldThrowOnNonUtilityConstructor() {
    assertFalse(PrivateConstructorsUtil.isUtilityConstructor(TestClassWithoutArguments.class));
  }

  @Test
  void shouldDetectExceptionCause() {
    assertTrue(
        PrivateConstructorsUtil.throwsOnCall(
            IllegalArgumentException.class, TestClassThrowingException.class));
  }

  @Test
  void shouldDetectUnexpectedExceptionCause() {
    assertFalse(
        PrivateConstructorsUtil.throwsOnCall(
            NullPointerException.class, TestClassThrowingException.class));
  }

  private static class TestClassPublicConstructor {
    public TestClassPublicConstructor() {}
  }

  private static class TestClassWithoutArguments {
    private TestClassWithoutArguments(String text) {}
  }

  private static class TestClassThrowingException {
    private TestClassThrowingException() {
      throw new IllegalArgumentException("don't call me");
    }
  }
}
