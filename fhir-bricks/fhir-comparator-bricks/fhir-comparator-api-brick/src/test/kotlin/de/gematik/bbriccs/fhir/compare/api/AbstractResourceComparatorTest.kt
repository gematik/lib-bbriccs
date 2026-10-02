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

import de.gematik.bbriccs.fhir.compare.trans.TestSupporter
import de.gematik.bbriccs.utils.ResourceLoader
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.r4.model.Resource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AbstractResourceComparatorTest {
  private val expected =
    TestSupporter.readJsonResource(
      ResourceLoader.getFileFromResource(
        "expected/valid-example-case-01-digitaler-durchschlag.json",
      ).toPath(),
    )

  @Test
  fun shouldCompare() {
    val comparator = TestResourceComparator()
    comparator.compare(expected, expected)
    assertEquals("okay", comparator.input.toString())
  }

  @Test
  fun shouldReturnNullInstance() {
    val comparator = TestResourceComparator()
    val result = comparator.normalize(null, "/")
    assertTrue(result.isJsonNull)
  }
}

class TestResourceComparator : AbstractResourceComparator() {
  val input = StringBuilder()

  override fun compare(actual: Resource, expected: Resource): Parameters {
    toCanonicalJsonNode(actual)
    toCanonicalJsonNode(expected)

    val actual = toCanonicalResource(expected)
    val expected = toCanonicalResource(expected)
    val result = actual.equalsShallow(expected)
    if (result) {
      input.append("okay")
    }
    return expected as Parameters
  }
}
