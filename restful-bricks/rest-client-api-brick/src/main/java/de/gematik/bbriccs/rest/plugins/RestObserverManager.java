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
 */

package de.gematik.bbriccs.rest.plugins;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RestObserverManager {

  private final List<HttpBRequestObserver> requestObservers;
  private final List<HttpBResponseObserver> responseObservers;

  protected RestObserverManager(RestObserverBuilder builder) {
    this.requestObservers = builder.requestObservers;
    this.responseObservers = builder.responseObservers;
  }

  public void serveRequestObservers(HttpBRequest request) {
    if (!this.requestObservers.isEmpty())
      this.requestObservers.parallelStream()
          .forEach(ro -> CompletableFuture.runAsync(() -> ro.onRequest(request)).join());
  }

  public void serveResponseObservers(HttpBResponse response) {
    if (!this.responseObservers.isEmpty())
      this.responseObservers.parallelStream()
          .forEach(ro -> CompletableFuture.runAsync(() -> ro.onResponse(response)).join());
  }

  public static class RestObserverBuilder {
    private final List<HttpBRequestObserver> requestObservers = new LinkedList<>();
    private final List<HttpBResponseObserver> responseObservers = new LinkedList<>();

    public RestObserverBuilder registerForRequests(HttpBRequestObserver ro) {
      this.requestObservers.add(ro);
      return this;
    }

    public RestObserverBuilder registerForResponses(HttpBResponseObserver ro) {
      this.responseObservers.add(ro);
      return this;
    }

    public RestObserverManager build() {
      return new RestObserverManager(this);
    }
  }
}
