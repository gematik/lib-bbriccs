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

import de.gematik.bbriccs.toggle.FeatureConfiguration
import de.gematik.bbriccs.utils.ResourceLoader
import org.hl7.fhir.r4.formats.JsonParser
import org.hl7.fhir.r4.formats.XmlParser
import org.hl7.fhir.r4.model.Resource
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.extension
import kotlin.io.path.notExists

object TestSupporter {

  private const val WRITE_ARTIFACTS_IS_ACTIVE = "write.Artifacts"

  private val xmlParser = XmlParser()
  private val jsonParser = JsonParser()

  @Throws(IOException::class)
  fun readXmlResource(path: Path): Resource = xmlParser.parse(Files.readString(path, StandardCharsets.UTF_8))

  @Throws(IOException::class)
  fun readJsonResource(path: Path): Resource = jsonParser.parse(Files.readString(path, StandardCharsets.UTF_8))

  @Throws(IOException::class)
  fun loadFourSourceResources(baseDir: String): List<Resource> = ResourceLoader.getResourceFilesInDirectory(baseDir, false)
    .filter { it.extension.lowercase(Locale.ROOT) in setOf("xml", "json") }
    .sortedBy(File::getPath)
    .map(File::toPath)
    .map(::readResource)

  private fun isXmlOrJsonFile(path: Path): Boolean = path.extension.lowercase(Locale.ROOT) in setOf("xml", "json")

  private fun readResource(path: Path): Resource = try {
    if (path.extension.lowercase(Locale.ROOT) == "xml") {
      readXmlResource(path)
    } else {
      readJsonResource(path)
    }
  } catch (exception: IOException) {
    throw IllegalStateException("Unable to read test resource: $path", exception)
  }

  @Throws(IOException::class)
  fun writeResource(resource: Resource, outputPath: Path) {
    if (!isWriteArtifactsEnabled()) return
    outputPath.parent
      ?.takeIf(Path::notExists)
      ?.let(Files::createDirectories)
    Files.writeString(outputPath, jsonParser.composeString(resource), StandardCharsets.UTF_8)
  }

  private fun isWriteArtifactsEnabled(): Boolean {
    val featureConfig = FeatureConfiguration()
    return featureConfig.getBooleanToggle(WRITE_ARTIFACTS_IS_ACTIVE, false)
  }
}
