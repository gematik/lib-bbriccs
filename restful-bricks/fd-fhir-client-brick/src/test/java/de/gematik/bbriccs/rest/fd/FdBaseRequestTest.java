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

package de.gematik.bbriccs.rest.fd;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.fd.query.QueryParameter;
import de.gematik.bbriccs.rest.fd.query.SearchPrefix;
import de.gematik.bbriccs.rest.fd.query.SearchQueryParameter;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FdBaseRequestTest {

  @ParameterizedTest
  @ValueSource(strings = {"task", "/task"})
  void shouldCalculatePath(String resource) {
    val tr = new TestFdRequest01(HttpRequestMethod.POST, resource);
    assertEquals(HttpRequestMethod.POST, tr.getMethod());
    assertEquals("/task", tr.getResourcePath());
    assertEquals(
        tr.getResourcePath(), tr.getRequestLocator()); // because no Query Parameters were given
    assertTrue(tr.getHeaderParameters().isEmpty());
    assertTrue(tr.getHeaders().isEmpty());
  }

  @Test
  void shouldCalculateLocatorWithQuery() {
    List<QueryParameter> qp =
        List.of(
            new SearchQueryParameter("authoredOn", SearchPrefix.GE, "2023"),
            new SearchQueryParameter("updated", SearchPrefix.EQ, "2024"));
    val tr = new TestFdRequest01(qp);
    assertEquals("/task", tr.getResourcePath());
    assertEquals("/task?authoredOn=ge2023&updated=eq2024", tr.getRequestLocator());
  }

  @Test
  void shouldCalculateLocatorWithQueryAndId() {
    List<QueryParameter> qp =
        List.of(
            new SearchQueryParameter("authoredOn", SearchPrefix.GE, "2023"),
            new SearchQueryParameter("updated", SearchPrefix.EQ, "2024"));
    val tr = new TestFdRequest01("890567", qp);
    assertEquals("/task/890567", tr.getResourcePath());
    assertEquals("/task/890567?authoredOn=ge2023&updated=eq2024", tr.getRequestLocator());
  }

  private static class TestFdRequest01 extends FdBaseRequest<EmptyResource, Task> {

    protected TestFdRequest01(HttpRequestMethod httpMethod, String fhirResource) {
      super(Task.class, httpMethod, fhirResource);
    }

    protected TestFdRequest01(List<QueryParameter> qp) {
      this(HttpRequestMethod.POST, "/task");
      this.queryParameters.addAll(qp);
    }

    protected TestFdRequest01(String resourceId, List<QueryParameter> qp) {
      super(Task.class, HttpRequestMethod.POST, "/task", resourceId);
      this.queryParameters.addAll(qp);
    }

    @Override
    public EmptyResource getRequestBody() {
      return new EmptyResource();
    }
  }
}
