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

import de.gematik.bbriccs.utils.ResourceLoader
import org.hl7.fhir.utilities.npm.NpmPackage
import org.hl7.fhir.validation.ValidationEngine
import org.slf4j.LoggerFactory
import tools.jackson.core.type.TypeReference
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule

object StructureMapsTransformerFactory {

  private val log = LoggerFactory.getLogger(StructureMapsTransformerFactory::class.java)

  @JvmStatic
  fun buildFor(name: String): StructureMapsTransformer {
    val yamlString: String = ResourceLoader.readFileFromResource("fhir/structureMaps/smaps-configuration.yaml")
    val om = YAMLMapper.builder().addModule(kotlinModule()).build()
    val configs = om.readValue(
      yamlString,
      object : TypeReference<List<StructureMapsConfiguration>>() {},
    )

    val config = configs.firstOrNull { it.name == name }
      ?: throw IllegalArgumentException("No StructureMaps configuration found for name: $name")

    val sanitizedCorePackage = requireNonBlank(config.corePackage, "corePackage")
    val sanitizedIgPath = requireNonBlank(config.igResourcePath, "igResourcePath")

    val validationEngine =
      ValidationEngine.ValidationEngineBuilder().withNoTerminologyServer()
        .fromSource(sanitizedCorePackage)

    val inputStream = StructureMapsTransformerFactory::class.java.getResourceAsStream(sanitizedIgPath)
    requireNotNull(inputStream) { "IG package resource not found: ${config.igResourcePath}" }
    inputStream.use {
      val npmPackage = NpmPackage.fromPackage(it)
      log.trace("Parsed: {}#{}", npmPackage.name(), npmPackage.version())
      validationEngine.igLoader.loadPackage(npmPackage, true)
    }
    return StructureMapsTransformer(validationEngine, config.url)
  }

  private fun requireNonBlank(value: String, name: String): String = value.trim().also { sanitized ->
    require(sanitized.isNotEmpty()) { "$name must not be blank" }
  }
}
