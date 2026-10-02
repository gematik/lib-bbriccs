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

package de.gematik.bbriccs.fhir.validation.support;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;

class IgnoredValueSetsSupportTest {

  @Test
  void shouldNotSupportNullValueSets() {
    val valueSetUrl = "test/ValueSet/test-vs";
    val ctx = FhirContext.forR4();
    val filter = new IgnoredValueSetsSupport(ctx, "test validator", List.of());

    val support = mock(IValidationSupport.class);
    val vsc = mock(ValidationSupportContext.class);
    when(vsc.getRootValidationSupport()).thenReturn(support);
    when(vsc.getRootValidationSupport()).thenReturn(support);
    when(support.fetchValueSet(valueSetUrl)).thenReturn(new ValueSet().setUrl(valueSetUrl));

    assertFalse(filter.isValueSetSupported(vsc, null));
  }

  @Test
  void shouldIgnoreValueSets() {
    val valueSetUrl = "test/ValueSet/test-vs";
    val ctx = FhirContext.forR4();
    val support = mock(IValidationSupport.class);
    val filter = new IgnoredValueSetsSupport(ctx, "test validator", List.of(valueSetUrl));

    val vsc = mock(ValidationSupportContext.class);
    when(vsc.getRootValidationSupport()).thenReturn(support);
    when(vsc.getRootValidationSupport()).thenReturn(support);
    when(support.fetchValueSet(valueSetUrl)).thenReturn(new ValueSet().setUrl(valueSetUrl));

    assertTrue(filter.isValueSetSupported(vsc, valueSetUrl));
  }

  @Test
  void shouldCodeFromValueSet() {
    val valueSetUrl = "test/ValueSet/test-vs";
    val ctx = FhirContext.forR4();
    val filter = new IgnoredValueSetsSupport(ctx, "test validator", List.of(valueSetUrl));

    val vsc = mock(ValidationSupportContext.class);
    val opts = mock(ConceptValidationOptions.class);

    val valueSet = new ValueSet().setUrl(valueSetUrl);

    val cvr =
        assertDoesNotThrow(
            () ->
                filter.validateCodeInValueSet(
                    vsc, opts, "test/CodeSystem/cs-1", "ABC", "test display", valueSet));
    assertTrue(cvr.isOk());
  }
}
