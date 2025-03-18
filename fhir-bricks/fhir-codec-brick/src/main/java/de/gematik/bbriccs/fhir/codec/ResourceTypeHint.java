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

package de.gematik.bbriccs.fhir.codec;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Resource;

public class ResourceTypeHint<T extends ProfileVersion, R extends Resource> {

  private final WithStructureDefinition<T> definition;
  private final List<T> versions;
  private final Class<R> mappingClass;

  private ResourceTypeHint(
      WithStructureDefinition<T> definition, Class<R> mappingClass, List<T> versions) {
    this.definition = definition;
    this.versions = versions;
    this.mappingClass = mappingClass;
  }

  public void register(FhirContext ctx) {
    // register to the default StructureDefinition without any version
    ctx.setDefaultTypeForProfile(definition.getCanonicalUrl(), mappingClass);
    // register the StructureDefinition for any of the given versions
    this.versions.forEach(
        v -> ctx.setDefaultTypeForProfile(definition.getVersionedUrl(v), mappingClass));
  }

  public static <T extends ProfileVersion> Builder<T> forStructure(
      WithStructureDefinition<T> definition) {
    return new Builder<>(definition);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder<T extends ProfileVersion> {

    private final WithStructureDefinition<T> definition;
    private final List<T> versions = new LinkedList<>();

    /**
     * Register the given specific versions to the mapping
     *
     * @param versions to selectively use for the mapping
     * @return the builder
     */
    @SafeVarargs
    public final Builder<T> forVersion(T... versions) {
      this.versions.addAll(Arrays.asList(versions));
      return this;
    }

    /**
     * Register the mapping for all versions of the given enum
     *
     * @param versionClass the enum class to use for all versions
     * @return the builder
     */
    public Builder<T> forAllVersionsFrom(Class<T> versionClass) {
      if (!versionClass.isEnum()) {
        throw new IllegalArgumentException(
            "Register ResourceTypeHint for all versions must be an enum");
      }
      return forVersion(versionClass.getEnumConstants());
    }

    /**
     * Generate the ResourceTypeHint for the given mapping class and the configured structure system
     * with the given version
     *
     * @param mappingClass which should be used to map the resource which map the configured
     *     structure system
     * @param <R> the resource type of the mapping class
     * @return the resource type hint
     */
    public <R extends Resource> ResourceTypeHint<T, R> mappingTo(Class<R> mappingClass) {
      return new ResourceTypeHint<>(definition, mappingClass, versions);
    }
  }
}
