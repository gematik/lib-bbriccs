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

package de.gematik.bbriccs.idp.request;

import de.gematik.bbriccs.idp.data.RedirectLocation;
import de.gematik.bbriccs.idp.data.RequestUri;
import de.gematik.bbriccs.rest.ApplicationGetRequest;
import de.gematik.bbriccs.rest.ErrorDataDefault;
import de.gematik.bbriccs.rest.HttpBResponse;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.val;

/**
 * Pushed Authorization Request (PAR)
 *
 * <p>gemSpec_ePA_FdV 6.1.3 (6) sendAuthorizationRequest (URI-PAR)
 */
public class GsiPushAuthRequest extends ApplicationGetRequest<RequestUri, ErrorDataDefault> {

  private GsiPushAuthRequest(String urlPath) {
    super(RequestUri.class, ErrorDataDefault.class, urlPath);
  }

  public static GsiPushAuthRequest from(RedirectLocation redirectLocation) {
    return new GsiPushAuthRequest(redirectLocation.getRelativePath());
  }

  @Override
  public Optional<Function<HttpBResponse, RequestUri>> customDecoder() {
    return Optional.of(
        response -> {
          // TODO: is there a better way than parsing the HTML?
          val parResponseData = response.bodyAsString(); // HTML??
          val codeRegex = "name=\"request_uri\" value=\"(.*)\"";

          val pattern = Pattern.compile(codeRegex, Pattern.MULTILINE);
          val matcher = pattern.matcher(parResponseData);

          matcher.find();
          return RequestUri.from(matcher.group(1));
        });
  }
}
