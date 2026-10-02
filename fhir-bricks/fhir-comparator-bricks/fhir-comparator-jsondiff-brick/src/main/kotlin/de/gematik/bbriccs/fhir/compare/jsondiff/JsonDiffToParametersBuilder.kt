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

package de.gematik.bbriccs.fhir.compare.jsondiff

import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.StringType
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

object JsonDiffToParametersBuilder {
  private val objectMapper = ObjectMapper()

  fun build(diff: JsonNode): Parameters {
    val parameters = Parameters()

    if (!diff.isArray) {
      return parameters
    }

    diff.forEach { op ->
      val operation = parameters.addParameter().setName("operation")

      op["op"]?.asString()?.let {
        operation.addPart().setName("op").value = StringType(it)
      }
      op["path"]?.asString()?.let {
        operation.addPart().setName("path").value = StringType(it)
      }
      op["from"]?.asString()?.let {
        operation.addPart().setName("from").value = StringType(it)
      }
      op["value"]?.let { valueNode ->
        operation.addPart().setName("value").value = toStringType(valueNode)
      }
    }

    return parameters
  }
  private fun toStringType(node: JsonNode): StringType {
    val text = if (node.isValueNode) node.asString() else objectMapper.writeValueAsString(node)
    return StringType(text)
  }
}
