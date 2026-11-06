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

package de.gematik.bbriccs.rest.vzd.auth;

import de.gematik.bbriccs.idp.data.ClientId;
import de.gematik.bbriccs.idp.data.CodeChallenge;
import de.gematik.bbriccs.idp.data.CodeChallengeMethod;
import de.gematik.bbriccs.idp.data.RedirectLocation;
import de.gematik.bbriccs.idp.data.RedirectUri;
import de.gematik.bbriccs.idp.data.ResponseType;
import de.gematik.bbriccs.idp.data.Scope;
import de.gematik.bbriccs.idp.data.State;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

@Getter
public class FhirVzdIdpRedirect extends RedirectLocation {

  private final ResponseType responseType;
  private final ClientId clientId;
  private final List<Scope> scopes;
  private final RedirectUri redirectUri;
  private final State state;
  private final CodeChallenge codeChallenge;
  private final CodeChallengeMethod codeChallengeMethod;

  private FhirVzdIdpRedirect(URL original) {
    super(original);
    this.responseType = this.getQueryParamOrThrow("response_type", ResponseType::from);
    this.clientId = this.getQueryParamOrThrow("client_id", ClientId::from);
    this.scopes = this.getQueryParamOrThrow("scope", Scope::parseFrom);
    this.redirectUri = this.getQueryParamOrThrow("redirect_uri", RedirectUri::from);
    this.state = this.getQueryParamOrThrow("state", State::from);
    this.codeChallenge = this.getQueryParamOrThrow("code_challenge", CodeChallenge::from);
    this.codeChallengeMethod =
        this.getQueryParamOrThrow("code_challenge_method", CodeChallengeMethod::from);
  }

  /**
   * this one is a shortcut for easier handling when using the de.gematik.idp.client
   *
   * @return a set of scopes
   */
  public Set<String> getScopesSet() {
    return new HashSet<>(this.getScopes().stream().map(Scope::value).toList());
  }

  public static FhirVzdIdpRedirect from(String location) {
    return RedirectLocation.from(location, FhirVzdIdpRedirect::new);
  }
}
