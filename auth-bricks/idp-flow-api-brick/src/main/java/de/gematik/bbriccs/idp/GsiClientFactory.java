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

package de.gematik.bbriccs.idp;

import de.gematik.bbriccs.idp.data.RedirectLocation;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

// TODO: too much?
public class GsiClientFactory {

  @Getter private final String idpIss;
  @Getter private final String redirectUri;
  private final List<HttpHeader> httpHeader;
  private final List<HttpBObserver> observers;

  private GsiClientFactory(GsiClientFactoryBuilder b) {
    this.idpIss = b.idpIss;
    this.redirectUri = b.redirectUri;
    this.httpHeader = b.httpHeader;
    this.observers = b.observers;
  }

  public GsiClient sessionClientFor(RedirectLocation redirectLocation) {
    val builder = GsiClient.forRedirect(redirectLocation);
    this.httpHeader.forEach(builder::header);
    this.observers.forEach(builder::register);

    return builder.withoutTlsVerification();
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class GsiClientFactoryBuilder {
    private final String idpIss;
    private final String redirectUri;
    private final List<HttpHeader> httpHeader = new ArrayList<>();
    private final List<HttpBObserver> observers = new ArrayList<>();

    public GsiClientFactoryBuilder header(HttpHeader... header) {
      this.httpHeader.addAll(Arrays.asList(header));
      return this;
    }

    public GsiClientFactoryBuilder register(HttpBObserver... rro) {
      this.observers.addAll(Arrays.asList(rro));
      return this;
    }
  }
}
