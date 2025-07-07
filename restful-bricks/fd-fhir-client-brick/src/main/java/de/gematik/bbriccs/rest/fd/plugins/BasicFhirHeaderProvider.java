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

import de.gematik.bbriccs.rest.fd.FhirBRequest;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Resource;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BasicFhirHeaderProvider implements FhirRequestHeaderProvider {

  private final Supplier<HttpHeader> httpHeaderSupplier;

  @Override
  public HttpHeader forRequest(FhirBRequest<? extends Resource, ? extends Resource> request) {
    return httpHeaderSupplier.get();
  }

  /**
   * Note: This is a temporary placeholder and will be replaced by a {@code Function<FhirRequest,
   * HttpHeader>} to be able to generate dynamic HttpHeaders depending on the FhirRequest.
   *
   * <p>Attention: if you are trying to set some sort of generic HttpHeader not related to FHIR, you
   * should consider using the {@link de.gematik.bbriccs.rest.plugins.RequestHeaderProvider} on the
   * underlying REST transport.
   *
   * @param httpHeaderSupplier to be performed
   * @return an HttpHeader
   */
  public static BasicFhirHeaderProvider simply(Supplier<HttpHeader> httpHeaderSupplier) {
    return new BasicFhirHeaderProvider(httpHeaderSupplier);
  }
}
