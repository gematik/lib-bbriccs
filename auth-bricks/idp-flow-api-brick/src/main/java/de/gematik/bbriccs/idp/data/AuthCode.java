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

import de.gematik.bbriccs.idp.exception.AuthException;
import java.net.URL;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

@Getter
public class AuthCode extends RedirectLocation {

  private final String code;
  private final String state;

  private AuthCode(AuthCodeBuilder b) {
    super(b.original);
    this.code = this.queryParams.get("code");
    this.state = this.queryParams.get("state");
  }

  public static AuthCodeBuilder from(String location) {
    try {
      val url = new URL(location);
      return from(url);
    } catch (Exception e) {
      throw new AuthException("Invalid URL: " + location, e);
    }
  }

  public static AuthCodeBuilder from(URL location) {
    return new AuthCodeBuilder(location);
  }

  @Setter
  @Accessors(fluent = true)
  public static class AuthCodeBuilder {

    private URL original;
    private Map<String, String> queryParams;

    private AuthCodeBuilder(URL original) {
      this.original = original;
      this.queryParams = RedirectLocation.parseQuery(original.getQuery());
    }

    public AuthCode build() {
      return new AuthCode(this);
    }
  }
}
