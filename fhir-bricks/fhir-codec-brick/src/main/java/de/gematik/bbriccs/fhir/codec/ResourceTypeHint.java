/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.bbriccs.fhir.codec;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.coding.ProfileStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import javax.annotation.Nullable;
import org.hl7.fhir.r4.model.Resource;

public class ResourceTypeHint<T extends ProfileVersion, R extends Resource> {

  private final ProfileStructureDefinition<T> definition;
  @Nullable private final T version;
  private final Class<R> mappingClass;

  private ResourceTypeHint(
      ProfileStructureDefinition<T> definition, @Nullable T version, Class<R> mappingClass) {
    this.definition = definition;
    this.version = version;
    this.mappingClass = mappingClass;
  }

  public void register(FhirContext ctx) {
    if (version != null) {
      // register StructureDefinition with full SemVer e.g., http://my.profile|1.2.3
      ctx.setDefaultTypeForProfile(definition.getVersionedUrl(version), mappingClass);
    } else {
      // register to the default StructureDefinition without any version
      ctx.setDefaultTypeForProfile(definition.getCanonicalUrl(), mappingClass);
    }
  }

  public static <T extends ProfileVersion, R extends Resource> ResourceTypeHint<T, R> forStructure(
      ProfileStructureDefinition<T> definition, @Nullable T version, Class<R> mappingClass) {
    return new ResourceTypeHint<>(definition, version, mappingClass);
  }

  public static <T extends ProfileVersion, R extends Resource> ResourceTypeHint<T, R> forStructure(
      ProfileStructureDefinition<T> definition, Class<R> mappingClass) {
    return new ResourceTypeHint<>(definition, null, mappingClass);
  }
}
