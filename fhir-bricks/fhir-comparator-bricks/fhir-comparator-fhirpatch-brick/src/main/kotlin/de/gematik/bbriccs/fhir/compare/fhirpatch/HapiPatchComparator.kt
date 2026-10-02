/*
 * Copyright (Change Date see Readme) gematik GmbH
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

package de.gematik.bbriccs.fhir.compare.fhirpatch

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.jpa.patch.FhirPatch
import de.gematik.bbriccs.fhir.compare.api.AbstractResourceComparator
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.Resource

class HapiPatchComparator : AbstractResourceComparator() {

  private val context = FhirContext.forR4()
  private val patch = FhirPatch(context)

  override fun compare(actual: Resource, expected: Resource): Parameters {
    val actual = toCanonicalResource(actual)
    val expected = toCanonicalResource(expected)
    return patch.diff(actual, expected) as Parameters
  }
}
