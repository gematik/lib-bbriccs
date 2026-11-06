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

import java.net.URL;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.val;

@Getter
public class AuthResponseRedirect extends RedirectLocation {

  private final String code;
  private final String state;

  private AuthResponseRedirect(URL original) {
    super(original);
    this.code = this.getQueryParamOrThrow("code");
    this.state = this.getQueryParamOrThrow("state");
  }

  public String asFormBody() {
    val params = new LinkedHashMap<String, String>();
    params.put("code", code);
    params.put("state", state);
    return FormUrlEncoding.encode(params);
  }

  public static AuthResponseRedirect from(String location) {
    return from(location, AuthResponseRedirect::from);
  }

  public static AuthResponseRedirect from(URL location) {
    return from(location, AuthResponseRedirect::new);
  }
}
