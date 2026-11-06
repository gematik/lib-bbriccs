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

import jakarta.annotation.Nullable;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.val;

@Getter
public class AuthRequestRedirect extends RedirectLocation {

  private final ClientId clientId;
  @Nullable private final State state;
  private final RequestUri requestUri;

  private AuthRequestRedirect(URL original) {
    super(original);
    this.clientId = this.getQueryParamOrThrow("client_id", ClientId::from);
    this.requestUri = this.getQueryParamOrThrow("request_uri", RequestUri::from);
    this.state = this.getQueryParam("state", State::from).orElse(null);
  }

  public Optional<State> getState() {
    return Optional.ofNullable(this.state);
  }

  public URI getAuthorizationUriFor(String userId) {
    val queryParams = Map.of("request_uri", this.requestUri.value(), "user_id", userId);
    return FormUrlEncoding.appendQuery(this.withoutQuery(), queryParams);
  }

  public static AuthRequestRedirect from(String location) {
    return RedirectLocation.from(location, AuthRequestRedirect::from);
  }

  public static AuthRequestRedirect from(URL location) {
    return RedirectLocation.from(location, AuthRequestRedirect::new);
  }
}
