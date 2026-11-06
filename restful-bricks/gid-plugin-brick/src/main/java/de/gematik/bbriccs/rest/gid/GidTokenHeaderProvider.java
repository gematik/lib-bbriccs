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

package de.gematik.bbriccs.rest.gid;

import de.gematik.bbriccs.gid.GidClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import de.gematik.bbriccs.rest.plugins.HttpBRequestCustomizer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GidTokenHeaderProvider implements HttpBRequestCustomizer {

  private final GidClient gidClient;

  private String nestedJwt; // the current GID token
  private Instant tokenUpdated; // point in time when the IDP token was updated

  @Override
  public List<HttpHeader> forRequest(HttpBRequest request) {
    if (isTokenExpired()) {
      log.info("Refresh the GID Token");

      this.nestedJwt = this.gidClient.login().getNestedJwt();
      this.tokenUpdated = Instant.now();
    } else {
      log.info("GID Token is still valid, no need to refresh");
    }

    return List.of(JwtHeaderKey.AUTHORIZATION.createHeader(this.nestedJwt));
  }

  private boolean isTokenExpired() {
    boolean ret;
    if (this.nestedJwt == null) {
      ret = true; // actually not expired but hasn't been fetched yet
    } else {
      val now = Instant.now();
      val diff = Duration.between(this.tokenUpdated, now).getSeconds();
      ret = diff >= 3 * 60; // refresh the token after 3 minutes, even if it is valid for longer
    }
    return ret;
  }

  public static GidTokenHeaderProvider using(GidClient gidClient) {
    return new GidTokenHeaderProvider(gidClient);
  }
}
