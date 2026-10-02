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

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.Resource
import java.util.UUID

internal class SourceBundleBuilder {
  fun build(sources: List<Resource?>): Bundle = Bundle().apply {
    type = Bundle.BundleType.COLLECTION
    sources.filterNotNull().forEach { appendResource(this, it) }
  }

  private fun appendResource(target: Bundle, source: Resource) {
    when {
      appendDocumentEntriesIfApplicable(target, source) -> Unit
      appendParameterEntriesIfApplicable(target, source) -> Unit
      else -> addAsEntry(target, source, null)
    }
  }

  private fun appendDocumentEntriesIfApplicable(target: Bundle, source: Resource): Boolean {
    if (source !is Bundle || source.type != Bundle.BundleType.DOCUMENT) {
      return false
    }

    source.entry
      .filter { it.hasResource() }
      .forEach { addAsEntry(target, it.resource, it.fullUrl) }
    return true
  }

  private fun appendParameterEntriesIfApplicable(target: Bundle, source: Resource): Boolean {
    if (source !is Parameters) {
      return false
    }

    source.parameter
      .flatMap { it.part }
      .filter { it.hasResource() }
      .forEach { addAsEntry(target, it.resource, null) }
    return true
  }

  private fun addAsEntry(target: Bundle, resource: Resource, fullUrl: String?) {
    val entry = target.addEntry()
    entry.resource = resource

    if (!fullUrl.isNullOrBlank()) {
      entry.fullUrl = fullUrl
      return
    }

    entry.fullUrl = if (resource.hasIdElement() && resource.idElement.hasIdPart()) {
      "urn:uuid:${resource.idElement.idPart}"
    } else {
      "urn:uuid:${UUID.randomUUID()}"
    }
  }
}
