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

package de.gematik.bbriccs.rest.fd.plugins;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class FhirCodecObserverManagerTest {

  @Test
  void shouldNotifyFhirCodecObservers() {
    val observer = new TestFhirCodecObserver();

    val observerManager =
        new FhirCodecObserverManager.FhirObserverBuilder()
            .registerForDecode(observer)
            .registerForEncode(observer)
            .build();

    observerManager.serveDecoderObservers(Bundle.class, "", new Bundle());
    observerManager.serveEncoderObservers(new Bundle(), "");

    assertEquals(1, observer.decodeCounter);
    assertEquals(1, observer.encodeCounter);
  }

  private static class TestFhirCodecObserver implements FhirCodecObserver {

    private int decodeCounter = 0;
    private int encodeCounter = 0;

    @Override
    public <E extends Resource, R extends Resource> void onDecode(
        Class<E> expectedType, String content, R resource) {
      decodeCounter++;
    }

    @Override
    public <R extends Resource> void onEncode(R resource, String content) {
      encodeCounter++;
    }
  }
}
