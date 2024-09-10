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

package de.gematik.bbriccs.rest.plugins;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class RestObserverManagerTest {

  @Test
  void shouldNotifyRequestObservers() {
    val observer = new TestObserver();

    val observerManager =
        new RestObserverManager.RestObserverBuilder()
            .registerForResponses(observer)
            .registerForRequests(observer)
            .build();

    val request = new HttpBRequest(HttpRequestMethod.GET, "test");
    val response = new HttpBResponse(200, List.of(), "");

    observerManager.serveRequestObservers(request);
    observerManager.serveResponseObservers(response);

    assertEquals(1, observer.requestCount);
    assertEquals(1, observer.responseCount);
  }

  @Test
  void shouldNotServeIfNotRegistered() {
    // quite obvious, but still required for code-coverage
    val observer = new TestObserver();

    val observerManager =
        new RestObserverManager.RestObserverBuilder()
            //                    .registerForResponses(observer)
            //                    .registerForRequests(observer)
            .build();

    val request = new HttpBRequest(HttpRequestMethod.GET, "test");
    val response = new HttpBResponse(200, List.of(), "");

    observerManager.serveRequestObservers(request);
    observerManager.serveResponseObservers(response);

    // Note: observermanager serves asynchroneously, therefore, we need to wait for the observer to
    // finish
    assertEquals(200, response.statusCode());
    assertTrue(response.isEmptyBody());
    System.out.println(
        format("Spend some time to serve observers for response {0}", response.statusCode()));

    assertEquals(0, observer.requestCount);
    assertEquals(0, observer.responseCount);
  }

  private static class TestObserver implements HttpBObserver {

    private int requestCount = 0;
    private int responseCount = 0;

    @Override
    public void onRequest(HttpBRequest request) {
      requestCount++;
    }

    @Override
    public void onResponse(HttpBResponse response) {
      responseCount++;
    }
  }
}
