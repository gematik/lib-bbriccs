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

package de.gematik.bbriccs.fhir.ucum.builder;

import de.gematik.bbriccs.fhir.ucum.UcumCodeSystem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuantityBuilder {

  private final Quantity quantity;

  public static QuantityBuilder asUcumPackage() {
    return asUcum("{Package}");
  }

  public static QuantityBuilder asUcum(String code) {
    val q = new Quantity();
    q.setSystem(UcumCodeSystem.getSystem().getCanonicalUrl()).setCode(code);
    return new QuantityBuilder(q);
  }

  public Quantity withValue(int amount) {
    return this.quantity.setValue(amount);
  }
}
