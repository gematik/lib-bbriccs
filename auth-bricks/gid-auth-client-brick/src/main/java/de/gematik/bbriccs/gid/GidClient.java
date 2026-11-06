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

package de.gematik.bbriccs.gid;

import de.gematik.bbriccs.gid.config.GidAuthenticationConfig;
import de.gematik.bbriccs.gid.response.GidAccessTokenResult;
import de.gematik.bbriccs.idp.exception.AuthException;

/**
 * Client API for authenticating a user with a Gesundheits-ID (GID) through a 'sektorale IdP'.
 *
 * <p>The information required for authentication, including the user's KVNR, is configured when the
 * client is created. The login call does not require an electronic health card (eGK).
 */
public interface GidClient {

  /**
   * Authenticates the user with the information configured when this client was created.
   *
   * @return the encrypted access token, its decrypted payload, and the nested JWT
   * @throws AuthException if the GID authentication flow fails
   */
  GidAccessTokenResult login();

  /**
   * Initializes this client before authentication.
   *
   * <p>The default implementation performs no initialization and returns this client.
   *
   * @return this client, or an initialized client supplied by an implementation
   */
  default GidClient initialize() {
    return this;
  }

  /**
   * Creates a GID client configured with the supplied authentication settings.
   *
   * @param config all required authentication settings, including endpoints, client identifier,
   *     requested scopes, and the user's KVNR
   * @return a configured GID client
   */
  static GidClient create(GidAuthenticationConfig config) {
    return new GidClientImpl(config);
  }
}
