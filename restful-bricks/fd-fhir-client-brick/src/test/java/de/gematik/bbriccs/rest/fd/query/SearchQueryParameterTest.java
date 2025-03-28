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

package de.gematik.bbriccs.rest.fd.query;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SearchQueryParameterTest {

  @ParameterizedTest
  @MethodSource
  void shouldEncodeSearchQueryParameter(QueryParameter qp, String expectation) {
    assertEquals(expectation, qp.encode());
  }

  static Stream<Arguments> shouldEncodeSearchQueryParameter() {
    return Stream.of(
        Arguments.of(new SearchQueryParameter("created", SearchPrefix.EQ, "123"), "created=eq123"),
        Arguments.of(
            new SearchQueryParameter("authoredOn", SearchPrefix.GE, "2024"), "authoredOn=ge2024"),
        Arguments.of(new SearchQueryParameter("task", "890567"), "task=890567"));
  }
}
