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

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Resource;

public interface FdRequest<T extends Resource, R extends Resource> {

  /**
   * This is required (mostly for VAU) to define on which resource the request shall be executed.
   * E.g. [baseUrl]/[resourcePath]
   *
   * @return the FHIR Resource
   */
  String getFhirResource();

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  String getRequestLocator();

  /**
   * Defines which HTTP-Method (for the underlying HTTP Request) shall be used.
   *
   * @return the HTTP-Method of the Command
   */
  HttpRequestMethod getMethod();

  /**
   * Get a Map of required Header-Parameters for this specific command. By Default, an empty map is
   * returned, which indicates no Header-Parameters
   *
   * @return map of Header-Parameters
   */
  Map<String, String> getHeaderParameters();

  default List<HttpHeader> getHeaders() {
    return this.getHeaderParameters().entrySet().stream()
        .map(entry -> new HttpHeader(entry.getKey(), entry.getValue()))
        .toList();
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return aa FHIR-Resource for the Request-Body. If no request body should be provided, an {@link
   *     EmptyResource} must be provided
   */
  T getRequestBody();

  Class<R> expectedResponseType();
}
