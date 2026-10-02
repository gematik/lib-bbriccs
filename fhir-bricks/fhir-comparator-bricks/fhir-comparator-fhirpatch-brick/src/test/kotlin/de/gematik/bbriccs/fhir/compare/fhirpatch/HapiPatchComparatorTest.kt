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

import de.gematik.bbriccs.fhir.compare.trans.StructureMapsTransformerFactory
import de.gematik.bbriccs.fhir.compare.trans.TestSupporter
import de.gematik.bbriccs.utils.ResourceLoader
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.StringType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HapiPatchComparatorTest {

  private val sources = TestSupporter.loadFourSourceResources("sources")
  private val expected =
    TestSupporter.readJsonResource(
      ResourceLoader.getFileFromResource(
        "expected/valid-example-case-01-digitaler-durchschlag.json",
      ).toPath(),
    )
  private val transformer = StructureMapsTransformerFactory.buildFor("T-Rezept")

  @Test
  fun shouldFindDifferences() {
    val transformed = transformer.transform(sources, Parameters::class.java)
    val comparator = HapiPatchComparator()

    val diff = comparator.compare(transformed, expected)
    assertFalse(diff.isEmpty)
  }

  @Test
  fun shouldFindNoDifferences() {
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
    val transformed = transformer.transform(sources, Parameters::class.java, patch)
    val comparator = HapiPatchComparator()

    val diff = comparator.compare(transformed, expected)
    assertTrue(diff.isEmpty)
  }
}
