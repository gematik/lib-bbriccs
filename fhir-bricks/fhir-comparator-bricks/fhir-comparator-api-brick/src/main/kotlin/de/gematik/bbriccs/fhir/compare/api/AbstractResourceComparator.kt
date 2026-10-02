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

package de.gematik.bbriccs.fhir.compare.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.hl7.fhir.r4.formats.IParser
import org.hl7.fhir.r4.model.Resource
import kotlin.collections.component1
import kotlin.collections.component2

abstract class AbstractResourceComparator : ResourceComparator {

  private val fhirJsonParser = org.hl7.fhir.r4.formats.JsonParser().apply {
    outputStyle = IParser.OutputStyle.NORMAL
  }
  private val objectMapper = ObjectMapper()

  fun toCanonicalResource(resource: Resource): Resource {
    val json = fhirJsonParser.composeString(resource)
    val raw = JsonParser.parseString(json)
    val normalized = normalize(raw, "$")
    return fhirJsonParser.parse(normalized.asJsonObject)
  }
  fun toCanonicalJsonNode(resource: Resource): JsonNode {
    val json = fhirJsonParser.composeString(resource)
    val raw = JsonParser.parseString(json)
    val normalized = normalize(raw, "$")
    return objectMapper.readTree(normalized.toString())
  }

  fun normalize(element: JsonElement?, path: String): JsonElement {
    if (element == null || element.isJsonNull) {
      return JsonNull.INSTANCE
    }

    if (element.isJsonObject) {
      return element.asJsonObject.entrySet()
        .asSequence()
        .filterNot { (key, _) -> shouldIgnore(key, path) }
        .map { (key, value) -> key to normalize(value, "$path.$key") }
        .sortedBy { (key, _) -> key }
        .fold(JsonObject()) { normalized, (key, value) ->
          normalized.apply { add(key, value) }
        }
    }

    if (element.isJsonArray) {
      return element.asJsonArray
        .mapIndexed { index, item -> normalize(item, "$path[]$index") }
        .sortedBy(JsonElement::toString)
        .fold(JsonArray()) { normalizedArray, value ->
          normalizedArray.apply { add(value) }
        }
    }

    return element.deepCopy()
  }

  private fun shouldIgnore(fieldName: String, path: String): Boolean = fieldName == "id" || (path.endsWith(".meta") && fieldName in setOf("versionId", "lastUpdated"))
}
