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

package de.gematik.bbriccs.idp.data;

import de.gematik.bbriccs.rest.ApplicationData;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public interface OAuthParameter extends ApplicationData {
  String value();

  static <T extends Enum<T> & OAuthParameter> Optional<T> fromEnum(
      String value, Class<T> enumType) {
    return Arrays.stream(enumType.getEnumConstants())
        .filter(it -> it.value().equals(value))
        .findFirst();
  }

  static <T extends OAuthParameter> T fromUndefined(String value, Function<String, T> constructor) {
    return constructor.apply(value);
  }
}
