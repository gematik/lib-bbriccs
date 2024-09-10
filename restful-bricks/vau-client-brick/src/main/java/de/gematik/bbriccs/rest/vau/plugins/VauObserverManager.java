/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.bbriccs.rest.vau.plugins;

import de.gematik.bbriccs.rest.plugins.RestObserverManager;
import de.gematik.bbriccs.rest.vau.VauEncryptionEnvelope;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VauObserverManager extends RestObserverManager {

  private final List<VauRequestObserver> vauRequestObservers;
  private final List<VauResponseObserver> vauResponseObservers;

  private VauObserverManager(VauObserverBuilder builder) {
    super(builder);
    this.vauRequestObservers = builder.vauRequestObservers;
    this.vauResponseObservers = builder.vauResponseObservers;
  }

  public void serveRequestObservers(VauEncryptionEnvelope request) {
    if (!this.vauRequestObservers.isEmpty())
      this.vauRequestObservers.parallelStream()
          .forEach(vro -> CompletableFuture.runAsync(() -> vro.onRequest(request)).join());
  }

  public void serveResponseObservers(VauEncryptionEnvelope response) {
    if (!this.vauResponseObservers.isEmpty())
      this.vauResponseObservers.parallelStream()
          .forEach(vro -> CompletableFuture.runAsync(() -> vro.onResponse(response)).join());
  }

  public static class VauObserverBuilder extends RestObserverBuilder {
    private final List<VauRequestObserver> vauRequestObservers = new LinkedList<>();
    private final List<VauResponseObserver> vauResponseObservers = new LinkedList<>();

    public VauObserverBuilder registerForRequests(VauRequestObserver vro) {
      this.vauRequestObservers.add(vro);
      return this;
    }

    public VauObserverBuilder registerForResponses(VauResponseObserver vro) {
      this.vauResponseObservers.add(vro);
      return this;
    }

    @Override
    public VauObserverManager build() {
      return new VauObserverManager(this);
    }
  }
}
