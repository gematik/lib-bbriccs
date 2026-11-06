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
public class AuthCode extends RedirectLocation {

  // Question: why does the code not have a wrapper class for type safety? (like ClientId, State,
  // etc.)
  private final String code;
  private final State state;

  private AuthCode(URL original) {
    super(original);
    this.code = this.getQueryParamOrThrow("code");

    // Note: state is optional/conditionally required, throwing an exception if not present might
    // not be ideal in all cases
    this.state = this.getQueryParamOrThrow("state", State::from);
  }

  public String asFormBody(ClientId clientId, RedirectUri redirectUri, String keyVerifier) {
    val grantType = GrantType.AUTHORIZATION_CODE;

    val params = new LinkedHashMap<String, String>();
    params.put("code", code);
    params.put("grant_type", grantType.value());
    params.put("client_id", clientId.value());
    params.put("redirect_uri", redirectUri.value());
    params.put("key_verifier", keyVerifier);

    return FormUrlEncoding.encode(params);
  }

  public static AuthCode from(String location) {
    return from(location, AuthCode::from);
  }

  public static AuthCode from(URL location) {
    return from(location, AuthCode::new);
  }
}
