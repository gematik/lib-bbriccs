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

package de.gematik.bbriccs.fhir.compare.trans

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.jpa.patch.FhirPatch
import org.hl7.fhir.r4.formats.JsonParser
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r5.elementmodel.Element
import org.hl7.fhir.r5.elementmodel.Manager
import org.hl7.fhir.r5.formats.JsonCreatorGson
import org.hl7.fhir.utilities.ByteProvider
import org.hl7.fhir.validation.ValidationEngine
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class StructureMapsTransformer internal constructor(
  val validationEngine: ValidationEngine,
  val structureMapUrl: String,
) {

  private val r4JsonParser = JsonParser()
  private val fhirPatch = FhirPatch(FhirContext.forR4())

  fun <T : Resource> transform(sources: List<Resource>, expectedClass: Class<T>): T = transform(sources, expectedClass, null)

  fun <T : Resource> transform(sources: List<Resource>, expectedClass: Class<T>, patch: Parameters?): T {
    val bundle = SourceBundleBuilder().build(sources)
    val sourceJson = r4JsonParser.composeBytes(bundle)

    val transformed = validationEngine.transform(
      ByteProvider.forBytes(sourceJson),
      Manager.FhirFormat.JSON,
      structureMapUrl,
    )
    val resource = parseR4Resource(transformed)
    if (patch != null) {
      fhirPatch.apply(resource, patch)
    }
    val parameters = expectedClass.cast(resource)!!
    return parameters
  }

  private fun parseR4Resource(transformed: Element): Resource {
    try {
      ByteArrayOutputStream().use { output ->
        OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
          val creator = JsonCreatorGson(writer)
          org.hl7.fhir.r5.elementmodel.JsonParser(validationEngine.context).compose(transformed, creator)
          creator.finish()
          writer.flush()
        }
        return r4JsonParser.parse(output.toByteArray())
      }
    } catch (exception: Exception) {
      throw IllegalStateException("Unable to parse transformed resource as R4", exception)
    }
  }
}
