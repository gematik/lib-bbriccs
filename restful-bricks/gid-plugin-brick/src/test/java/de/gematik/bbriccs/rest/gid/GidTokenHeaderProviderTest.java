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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.gid.GidClient;
import de.gematik.bbriccs.gid.response.GidAccessTokenResult;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.headers.JwtHeaderKey;
import lombok.val;
import org.junit.jupiter.api.Test;

class GidTokenHeaderProviderTest {

  /**
   * Good case without any HTTP traffic: the {@link GidClient} is mocked to return a fixed nested
   * JWT. The provider must log in once, emit it as {@code Authorization} header, and reuse the
   * cached token for a subsequent request instead of logging in again.
   */
  @Test
  void shouldProvideAuthorizationHeaderFromMockedGidClient() {
    val nestedJwt = "header.payload.signature";

    val gidClient = mock(GidClient.class);
    when(gidClient.login()).thenReturn(GidAccessTokenResult.builder().nestedJwt(nestedJwt).build());

    val provider = GidTokenHeaderProvider.using(gidClient);
    val request = mock(HttpBRequest.class);

    val headers = provider.forRequest(request);
    assertEquals(1, headers.size());
    assertEquals(JwtHeaderKey.AUTHORIZATION.createHeader(nestedJwt), headers.getFirst());

    val cachedHeaders = provider.forRequest(request);
    assertEquals(headers, cachedHeaders);
    verify(gidClient, times(1)).login();
  }
}
