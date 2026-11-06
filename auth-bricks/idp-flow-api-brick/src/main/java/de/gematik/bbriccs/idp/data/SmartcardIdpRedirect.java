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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

// TODO: check if there's a more generic term for this
// TODO: check which of these fields are required/optional
@Getter
public class SmartcardIdpRedirect extends RedirectLocation {

  private final Nonce nonce;
  private final State state;
  private final ClientId clientId;
  private final RedirectUri redirectUri;
  private final CodeChallenge codeChallenge;
  private final CodeChallengeMethod codeChallengeMethod;

  private SmartcardIdpRedirect(URL original) {
    super(original);
    this.nonce = this.getQueryParamOrThrow("nonce", Nonce::from);
    this.state = this.getQueryParamOrThrow("state", State::from);
    this.clientId = this.getQueryParamOrThrow("client_id", ClientId::from);
    this.redirectUri = this.getQueryParamOrThrow("redirect_uri", RedirectUri::from);
    this.codeChallenge = this.getQueryParamOrThrow("code_challenge", CodeChallenge::from);
    this.codeChallengeMethod =
        this.getQueryParamOrThrow("code_challenge_method", CodeChallengeMethod::from);
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

  public static SmartcardIdpRedirect from(String location) {
    return RedirectLocation.from(location, SmartcardIdpRedirect::new);
  }
}
