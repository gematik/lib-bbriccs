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

package de.gematik.bbriccs.rest.vau;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import java.net.http.HttpClient;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RawHttpCodecTest {

  @ParameterizedTest
  @MethodSource
  void shouldProvideHttpVersionAsString(HttpClient.Version version, String expectation) {
    val codec = new RawTestHttpCodecImpl(version);
    assertEquals(expectation, codec.getHttpVersionString());
  }

  static Stream<Arguments> shouldProvideHttpVersionAsString() {
    return Stream.of(
        Arguments.of(HttpClient.Version.HTTP_1_1, "HTTP/1.1"),
        Arguments.of(HttpClient.Version.HTTP_2, "HTTP/2"));
  }

  private record RawTestHttpCodecImpl(HttpClient.Version httpVersion) implements RawHttpCodec {

    @Override
    public String encode(HttpBRequest request) {
      return null;
    }

    @Override
    public HttpBResponse decode(byte[] rawResponse) {
      return null;
    }
  }
}
