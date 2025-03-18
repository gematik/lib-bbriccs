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

package de.gematik.bbriccs.rest.fd.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.hl7.fhir.r4.model.Resource;

public class FhirCodecObserverManager {
  private final List<FhirEncoderObserver> encodeObservers;
  private final List<FhirDecoderObserver> decodeObservers;

  protected FhirCodecObserverManager(FhirObserverBuilder builder) {
    this.encodeObservers = builder.encodeObservers;
    this.decodeObservers = builder.decodeObservers;
  }

  public <R extends Resource> void serveEncoderObservers(R resource, String content) {
    if (!this.encodeObservers.isEmpty())
      this.encodeObservers.parallelStream()
          .forEach(ro -> CompletableFuture.runAsync(() -> ro.onEncode(resource, content)).join());
  }

  public <E extends Resource, R extends Resource> void serveDecoderObservers(
      Class<E> expectedType, String content, R resource) {
    if (!this.decodeObservers.isEmpty())
      this.decodeObservers.parallelStream()
          .forEach(
              ro ->
                  CompletableFuture.runAsync(() -> ro.onDecode(expectedType, content, resource))
                      .join());
  }

  public static class FhirObserverBuilder {

    private final List<FhirEncoderObserver> encodeObservers = new LinkedList<>();
    private final List<FhirDecoderObserver> decodeObservers = new LinkedList<>();

    public FhirObserverBuilder registerForEncode(FhirEncoderObserver feo) {
      this.encodeObservers.add(feo);
      return this;
    }

    public FhirObserverBuilder registerForDecode(FhirDecoderObserver fdo) {
      this.decodeObservers.add(fdo);
      return this;
    }

    public FhirCodecObserverManager build() {
      return new FhirCodecObserverManager(this);
    }
  }
}
