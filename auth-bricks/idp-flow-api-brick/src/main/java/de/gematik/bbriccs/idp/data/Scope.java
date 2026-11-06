/*
 * Copyright (Change Date see Readme) gematik GmbH
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

public interface Scope {

  String value();

  static Scope openId() {
    return DefaultScope.OPENID;
  }

  static Scope forValue(String value) {
    return new CustomScope(value);
  }

  /**
   * Parses a raw form-encoded scope parameter. Decodes once before splitting on ASCII spaces (RFC
   * 6749 section 3.3), preserving literal plus signs encoded as {@code %2B}.
   */
  static List<Scope> parseFrom(String scopes) {
    val tokens = FormUrlEncoding.decodeValue(scopes).split(" +");
    return Stream.of(tokens).filter(token -> !token.isEmpty()).map(Scope::forValue).toList();
  }

  static List<Scope> openId(Scope... scopes) {
    val result = new ArrayList<Scope>();
    result.add(DefaultScope.OPENID);
    result.addAll(List.of(scopes));
    return result;
  }

  enum DefaultScope implements Scope {
    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),
    ADDRESS("address"),
    PHONE("phone");

    private final String value;

    DefaultScope(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return this.value;
    }
  }

  class CustomScope implements Scope {
    @Getter
    @Accessors(fluent = true)
    private final String value;

    private CustomScope(String value) {
      this.value = value;
    }
  }
}
