/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.bbriccs.fhir.builder;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceLineItemPriceComponentComponent;
import org.junit.jupiter.api.Test;

class ElementBuilderTest {

  @Test
  void shouldSetDefaultIdOnElementBuilder() {
    val pc = PriceComponentBuilder.builder().build();
    assertNotNull(pc.getId());
  }

  @Test
  void shouldSetCustomIdOnElementBuilder() {
    val pc = PriceComponentBuilder.builder().setId("my-custom-id").build();
    assertNotNull(pc.getId());
    assertEquals("my-custom-id", pc.getId());
  }

  private static class PriceComponentBuilder
      extends ElementBuilder<InvoiceLineItemPriceComponentComponent, PriceComponentBuilder> {

    public static PriceComponentBuilder builder() {
      return new PriceComponentBuilder();
    }

    @Override
    public InvoiceLineItemPriceComponentComponent build() {
      val pc = new Invoice.InvoiceLineItemPriceComponentComponent();
      return setIdTo(pc);
    }
  }
}
