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

class FhirBaseBRequestTest {

  @ParameterizedTest
  @ValueSource(strings = {"task", "/task"})
  void shouldCalculatePath(String resource) {
    val tr = new TestFhirGetBRequest01(resource);
    assertEquals(HttpRequestMethod.GET, tr.getMethod());
    assertEquals("/task", tr.getResourcePath());

    // because no Query Parameters were given
    assertEquals(tr.getResourcePath(), tr.getRequestLocator());
    assertTrue(tr.getHeaderParameters().isEmpty());
    assertTrue(tr.getHeaders().isEmpty());
  }

  @Test
  void shouldCalculateLocatorWithQuery() {
    List<QueryParameter> qp =
        List.of(
            new SearchQueryParameter("authoredOn", SearchPrefix.GE, "2023"),
            new SearchQueryParameter("updated", SearchPrefix.EQ, "2024"));
    val tr = new TestFhirGetBRequest01(qp);
    assertEquals("/task", tr.getResourcePath());
    assertEquals("/task?authoredOn=ge2023&updated=eq2024", tr.getRequestLocator());
  }

  @Test
  void shouldCalculateLocatorWithQueryAndId() {
    List<QueryParameter> qp =
        List.of(
            new SearchQueryParameter("authoredOn", SearchPrefix.GE, "2023"),
            new SearchQueryParameter("updated", SearchPrefix.EQ, "2024"));
    val tr = new TestFhirGetBRequest01("890567", qp);
    assertEquals("/task/890567", tr.getResourcePath());
    assertEquals("/task/890567?authoredOn=ge2023&updated=eq2024", tr.getRequestLocator());
    assertEquals(EmptyResource.class, tr.getRequestBody().getClass());
  }

  private static class TestFhirGetBRequest01 extends FhirGetBRequest<Task> {
    protected TestFhirGetBRequest01(String fhirResource) {
      super(Task.class, fhirResource);
    }

    protected TestFhirGetBRequest01(List<QueryParameter> qp) {
      super(Task.class, "/task", null, qp);
    }

    protected TestFhirGetBRequest01(String resourceId, List<QueryParameter> qp) {
      super(Task.class, "/task", resourceId, qp);
    }
  }
}
