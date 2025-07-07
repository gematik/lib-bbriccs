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

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.fd.query.QueryParameter;
import java.util.List;
import javax.annotation.Nullable;
import org.hl7.fhir.r4.model.Resource;

public abstract class FhirGetBRequest<R extends Resource>
    extends FhirBaseBRequest<EmptyResource, R> {

  protected FhirGetBRequest(Class<R> expect, String fhirResource) {
    super(expect, HttpRequestMethod.GET, fhirResource);
  }

  protected FhirGetBRequest(Class<R> expect, String fhirResource, @Nullable String resourceId) {
    super(expect, HttpRequestMethod.GET, fhirResource, resourceId);
  }

  protected FhirGetBRequest(
      Class<R> expect,
      String fhirResource,
      @Nullable String resourceId,
      List<QueryParameter> queryParameters) {
    this(expect, fhirResource, resourceId);
    this.queryParameters.addAll(queryParameters);
  }

  @Override
  public final EmptyResource getRequestBody() {
    return new EmptyResource();
  }
}
