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
import de.gematik.bbriccs.rest.ApplicationClient;
import de.gematik.bbriccs.rest.BasicHttpClient;
import de.gematik.bbriccs.rest.HttpClientBuilder;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.plugins.HttpBObserver;
import lombok.experimental.Delegate;
import lombok.val;

public class GsiClient implements ApplicationClient {

  @Delegate private final ApplicationClient applicationClient;
  private final RedirectLocation redirectLocation;

  private GsiClient(ApplicationClient applicationClient, RedirectLocation redirectLocation) {
    this.applicationClient = applicationClient;
    this.redirectLocation = redirectLocation;
  }

  public static GsiClientBuilder forRedirect(RedirectLocation redirectLocation) {
    return new GsiClientBuilder(redirectLocation);
  }

  public static class GsiClientBuilder {
    private final RedirectLocation redirectLocation;
    private final HttpClientBuilder<BasicHttpClient.BasicHttpClientBuilder> httpClientBuilder;

    private GsiClientBuilder(RedirectLocation redirectLocation) {
      this.redirectLocation = redirectLocation;
      this.httpClientBuilder = BasicHttpClient.forUrl(redirectLocation.getBaseUrl());
    }

    public GsiClientBuilder header(HttpHeader... header) {
      this.httpClientBuilder.header(header);
      return this;
    }

    public GsiClientBuilder register(HttpBObserver rro) {
      this.httpClientBuilder.register(rro);
      return this;
    }

    public GsiClientBuilder proxy(String host, int port) {
      this.httpClientBuilder.proxy(host, port);
      return this;
    }

    public GsiClient withoutTlsVerification() {
      val httpClient = this.httpClientBuilder.withoutTlsVerification();
      val applicationClient = ApplicationClient.using(httpClient).withSimpleObjectMapper();
      return new GsiClient(applicationClient, redirectLocation);
    }
  }
}
