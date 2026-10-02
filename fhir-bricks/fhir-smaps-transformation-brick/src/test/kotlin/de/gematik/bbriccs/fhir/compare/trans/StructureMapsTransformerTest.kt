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

import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.StringType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class StructureMapsTransformerTest {

  val sources = TestSupporter.loadFourSourceResources("source/valid")
  val invalidSources = TestSupporter.loadFourSourceResources("source/invalid")

  @Test
  fun shouldThrowOnInvalidStructureMap() {
    assertThrows<IllegalArgumentException> {
      StructureMapsTransformerFactory.buildFor("Invalid-StructureMap")
    }
  }

  @Test
  fun shouldNotThrowOnTransforming() {
    val transformer = StructureMapsTransformerFactory.buildFor("T-Rezept")
    assertDoesNotThrow { transformer.transform(sources, Parameters::class.java) }
  }

  @Test
  fun shouldNotThrowOnTransformingWithPatch() {
    val transformer = StructureMapsTransformerFactory.buildFor("T-Rezept")

    val patch = Parameters()
    patch.addParameter().setName("operation").apply {
      addPart().setName("type").setValue(CodeType("add"))
      addPart().setName("path").setValue(StringType("Parameters.parameter.where(name='rxPrescription')"))
      addPart().setName("name").setValue(StringType("part"))
      addPart().setName("value").apply {
        addPart().setName("name").setValue(StringType("prescriptionSignatureDate"))
        addPart().setName("value").setValue(InstantType("2026-04-01T08:23:12Z"))
      }
    }
    assertDoesNotThrow { transformer.transform(sources, Parameters::class.java, patch) }
  }

  @Test
  fun shouldPickRandomUUIDWithSourcesWithoutId() {
    val transformer = StructureMapsTransformerFactory.buildFor("T-Rezept")
    assertDoesNotThrow { transformer.transform(invalidSources, Parameters::class.java) }
  }
}
