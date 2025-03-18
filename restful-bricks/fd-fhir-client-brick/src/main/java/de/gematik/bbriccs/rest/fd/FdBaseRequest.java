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

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.fd.query.QueryParameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Resource;

/**
 * @param <R> the type of the expected Response Body
 */
public abstract class FdBaseRequest<T extends Resource, R extends Resource>
    implements FdRequest<T, R> {

  private final Class<R> expectedResponse;
  private final HttpRequestMethod httpRequestMethod;
  private final String fhirResource;
  private final String resourceId;

  protected final List<QueryParameter> queryParameters;
  protected final Map<String, String> headerParameters;

  protected FdBaseRequest(Class<R> expect, HttpRequestMethod method, String fhirResource) {
    this(expect, method, fhirResource, null);
  }

  protected FdBaseRequest(
      Class<R> expect, HttpRequestMethod method, String fhirResource, String resourceId) {
    this.expectedResponse = expect;
    this.httpRequestMethod = method;
    this.fhirResource = fhirResource;
    this.resourceId = resourceId;
    this.queryParameters = new ArrayList<>();
    this.headerParameters = new HashMap<>();
  }

  /**
   * This is required (mostly for VAU) to define on which resource the request shall be executed.
   *
   * @return the FHIR Resource
   */
  @Override
  public final String getFhirResource() {
    return fhirResource.startsWith("/") ? fhirResource : "/" + fhirResource;
  }

  /**
   * This method provides the full Path to a Resource for a Request. Example: /Task if Task was
   * provided without an ID and /Task/[id] if an ID was provided
   *
   * @return the Path
   */
  protected final String getResourcePath() {
    String ret = this.getFhirResource();
    if (resourceId != null) {
      ret += "/" + resourceId;
    }
    return ret;
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + this.getQueryPart();
  }

  /**
   * Defines which HTTP-Method (for the inner-HTTP Request) shall be used.
   *
   * @return the HTTP-Method of the Command
   */
  public final HttpRequestMethod getMethod() {
    return httpRequestMethod;
  }

  /**
   * Get a Map of required Header-Parameters for this specific command. By Default, an empty map is
   * returned, which indicates no Header-Parameters
   *
   * @return map of Header-Parameters
   */
  public final Map<String, String> getHeaderParameters() {
    return headerParameters;
  }

  /**
   * What type of Response-Body does this command expect? This methode is required for the
   * FHIR-Codec to decode the Response-Body to a concrete FHIR-Resource
   *
   * @return the Type of the expected Response-Body
   */
  public final Class<R> expectedResponseType() {
    return expectedResponse;
  }

  protected String getQueryPart() {
    if (this.queryParameters.isEmpty()) {
      return "";
    }

    return "?"
        + this.queryParameters.stream()
            .map(QueryParameter::encode)
            .collect(Collectors.joining("&"));
  }
}
