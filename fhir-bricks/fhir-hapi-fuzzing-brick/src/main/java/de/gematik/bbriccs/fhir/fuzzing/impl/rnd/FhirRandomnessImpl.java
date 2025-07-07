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

package de.gematik.bbriccs.fhir.fuzzing.impl.rnd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.FhirRandomness;
import de.gematik.bbriccs.fhir.fuzzing.FhirType;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceFactory;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Type;

public class FhirRandomnessImpl implements FhirRandomness {

  private final Randomness randomness;

  public FhirRandomnessImpl(Randomness randomness) {
    this.randomness = randomness;
  }

  @Override
  public Resource createResource() {
    return createResource(randomness.chooseRandomFromEnum(ResourceType.class));
  }

  @Override
  public Resource createResource(ResourceType type) {
    return ResourceFactory.createResource(type.name());
  }

  @Override
  public Type createType() {
    return createType(randomness.chooseRandomFromEnum(FhirType.class));
  }

  @Override
  public Type createType(FhirType type) {
    return (Type) ResourceFactory.createType(type.getTypeName());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Type> T createType(Class<T> type) {
    return (T) createType(FhirType.fromClass(type));
  }

  @Override
  public String fhirResourceId() {
    return fhirResourceId(randomness.chooseRandomFromEnum(ResourceType.class));
  }

  @Override
  public String fhirResourceId(ResourceType resourceType) {
    return format("{0}/{1}", resourceType.name(), randomness.uuid());
  }
}
