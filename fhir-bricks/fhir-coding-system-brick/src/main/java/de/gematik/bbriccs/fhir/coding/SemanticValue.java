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
 */

package de.gematik.bbriccs.fhir.coding;

import static java.text.MessageFormat.format;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

@Getter
@EqualsAndHashCode
public abstract class SemanticValue<T, S extends WithSystem> {

  private final S system;
  private final T value; // NOSONAR Value<T> is wrapping the concrete T value with a naming system

  protected SemanticValue(S system, T value) {
    this.system = system;
    this.value = value;
  }

  public String getSystemUrl() {
    return this.system.getCanonicalUrl();
  }

  public String getValueAsString() {
    return format("{0}", this.getValue());
  }

  public Identifier asIdentifier() {
    return asIdentifier(this.system);
  }

  public Coding asCoding() {
    return this.getSystem().asCoding(this.getValueAsString());
  }

  public CodeableConcept asCodeableConcept() {
    return this.getSystem().asCodeableConcept(this.getValueAsString());
  }

  /**
   * This method is required because depending on the version of the used profile, the concrete
   * system can vary.
   *
   * @param system to be used to denote the identifier
   * @return this value as an identifier
   */
  public Identifier asIdentifier(S system) {
    return new Identifier()
        .setSystem(system.getCanonicalUrl())
        .setValue(format("{0}", this.getValue()));
  }

  public Reference asReference() {
    return asReference(this.system);
  }

  /**
   * This method is required because depending on the version of the used profile, the concrete
   * system can vary.
   *
   * @param system to be used to denote the identifier
   * @return this value as reference
   */
  public Reference asReference(S system) {
    val ref = new Reference();
    ref.setIdentifier(asIdentifier(system));
    return ref;
  }

  @Override
  public String toString() {
    return format(
        "{0}(System: {1} Value: {2})",
        this.getClass().getSimpleName(), this.system.getCanonicalUrl(), this.value);
  }
}
