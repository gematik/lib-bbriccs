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

import java.util.Optional;
import java.util.function.Function;

public interface FeatureToggle<T> {

  String getKey();

  Function<String, T> getConverter();

  T getDefaultValue();

  static boolean hasToggle(String key) {
    return new FeatureConfiguration().hasToggle(key);
  }

  static Optional<String> getStringToggle(String key) {
    return new FeatureConfiguration().getStringToggle(key);
  }

  static String getStringToggle(String key, String defaultValue) {
    return new FeatureConfiguration().getStringToggle(key, defaultValue);
  }

  static boolean getBooleanToggle(String key, boolean defaultValue) {
    return new FeatureConfiguration().getBooleanToggle(key, defaultValue);
  }

  static int getIntegerToggle(String key, int defaultValue) {
    return new FeatureConfiguration().getIntegerToggle(key, defaultValue);
  }
}
