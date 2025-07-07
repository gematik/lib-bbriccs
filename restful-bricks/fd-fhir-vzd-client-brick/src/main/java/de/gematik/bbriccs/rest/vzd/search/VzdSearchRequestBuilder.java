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

package de.gematik.bbriccs.rest.vzd.search;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.vzd.valueset.ProfessionOID;
import de.gematik.bbriccs.rest.fd.query.QueryParameter;
import de.gematik.bbriccs.rest.fd.query.SearchQueryParameter;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.val;

public interface VzdSearchRequestBuilder {

  static QueryParameter byProfession(ProfessionOID... professions) {
    val professionOids =
        Arrays.stream(professions).map(ProfessionOID::getCode).collect(Collectors.joining(","));

    return new SearchQueryParameter("organization.type", professionOids);
  }

  static String quote(String input) {
    return format("\"{0}\"", input);
  }
}
