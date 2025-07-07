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

package de.gematik.bbriccs.utils;

import java.lang.reflect.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrivateConstructorsUtil {

  private PrivateConstructorsUtil() {
    throw new IllegalAccessError("Utility class: don't use the constructor");
  }

  public static boolean isUtilityConstructor(Class<?> cls) {
    try {
      val constructor = cls.getDeclaredConstructor();
      if (Modifier.isPublic(constructor.getModifiers())) {
        return false;
      }

      return throwsOnCall(InvocationTargetException.class, cls);
    } catch (NoSuchMethodException nsme) {
      log.info(nsme.getMessage());
      return isExpectedError(InvocationTargetException.class, nsme);
    }
  }

  @SuppressWarnings("java:S3011")
  public static boolean throwsOnCall(Class<? extends Throwable> expectation, Class<?> cls) {
    try {
      val constructor = cls.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    } catch (Throwable e) {
      return isExpectedError(expectation, e);
    }
    return false;
  }

  private static boolean isExpectedError(Class<? extends Throwable> expectation, Throwable error) {
    return error.getClass().equals(expectation)
        || (error.getCause() != null && error.getCause().getClass().equals(expectation));
  }
}
