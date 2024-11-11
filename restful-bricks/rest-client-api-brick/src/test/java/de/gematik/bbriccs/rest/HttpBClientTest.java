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

package de.gematik.bbriccs.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class HttpBClientTest {

  @Test
  void shouldHaveDefaultInitShutDown() {
    val client = new TestHttpBClient();
    assertDoesNotThrow(client::init);
    assertDoesNotThrow(client::shutDown);
  }

  private static class TestHttpBClient implements HttpBClient {

    @Override
    public HttpBResponse send(HttpBRequest bRequest) {
      return new HttpBResponse(HttpVersion.HTTP_1_1, 200, List.of(), new byte[0]);
    }
  }
}
