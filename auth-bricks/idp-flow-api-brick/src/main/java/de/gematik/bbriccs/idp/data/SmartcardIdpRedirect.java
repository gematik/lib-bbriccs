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

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: check if there's a more generic term for this
// TODO: check which of these fields are required/optional
public class SmartcardIdpRedirect extends RedirectLocation {

  private SmartcardIdpRedirect(URL original) {
    super(original);
  }

  public List<String> getScopes() {
    return this.getQueryParam("scope")
        .map(it -> Arrays.asList(it.split("[+| ]")))
        .orElseGet(List::of);
  }

  /**
   * this one is a shortcut for easier handling when using the de.gematik.idp.client
   *
   * @return a set of scopes
   */
  public Set<String> getScopesSet() {
    return new HashSet<>(this.getScopes());
  }

  public Nonce getNonce() {
    return this.getQueryParam("nonce").map(Nonce::from).orElseThrow();
  }

  public ClientId getClientId() {
    return this.getQueryParam("client_id").map(ClientId::from).orElseThrow();
  }

  public RedirectUri getRedirectUri() {
    return this.getQueryParam("redirect_uri")
        .map(it -> URLDecoder.decode(it, StandardCharsets.UTF_8))
        .map(RedirectUri::from)
        .orElseThrow();
  }

  public CodeChallenge getCodeChallenge() {
    return this.getQueryParam("code_challenge").map(CodeChallenge::from).orElseThrow();
  }

  public CodeChallengeMethod getCodeChallengeMethod() {
    return this.getQueryParam("code_challenge_method").map(CodeChallengeMethod::from).orElseThrow();
  }

  public State getState() {
    return this.getQueryParam("state").map(State::from).orElseThrow();
  }

  public static SmartcardIdpRedirect from(String location) {
    return RedirectLocation.from(location, SmartcardIdpRedirect::new);
  }
}
