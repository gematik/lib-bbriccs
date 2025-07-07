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

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import java.util.List;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;

public abstract class ResourceBuilder<R extends Resource, B extends ResourceBuilder<R, B>>
    extends BaseBuilder<R, B> {

  /**
   * Sets the resource ID to the given element.
   *
   * <p>This method assigns either a unique, if no ID was given by the user, or the given ID to the
   * provided resource element. The ID is generated based on the implementation of {@link
   * BaseBuilder#getResourceId()}.
   *
   * @param resource the resource to which the ID will be assigned
   * @return the resource element with the assigned ID
   */
  @Override
  protected final R setIdTo(R resource) {
    resource.setId(this.getResourceId());
    return resource;
  }

  /**
   * Uses the given construction supplier to create the resource, sets a resource ID, and assigns
   * the given profile as the profile in the resource's meta-information without any version
   * information.
   *
   * @param constructor the supplier to instantiate the resource object
   * @param profile the profile to use as the profile in the resource's meta-information
   * @return the instantiated resource
   */
  protected final R createResource(Supplier<R> constructor, WithStructureDefinition<?> profile) {
    return createResource(constructor, profile.asCanonicalType());
  }

  /**
   * Uses the given construction supplier to create the resource, sets a resource ID, and assigns
   * the given profile and version as the profile in the resource's meta-information with the given
   * version.
   *
   * @param constructor the supplier to instantiate the resource object
   * @param profile the profile to use as the profile in the resource's meta-information
   * @param version the version of the profile in the resource's meta-information
   * @return the instantiated resource
   */
  protected final <V extends ProfileVersion> R createResource(
      Supplier<R> constructor, WithStructureDefinition<V> profile, V version) {
    val profileCanonical = profile.asCanonicalType(version);
    return createResource(constructor, profileCanonical);
  }

  /**
   * Uses the given construction supplier to create the resource, sets a resource ID, and assigns
   * the given canonical type as the profile in the resource's meta-information.
   *
   * @param constructor the supplier to instantiate the resource object
   * @param profile the canonical type to use as the profile in the resource's meta-information
   * @return the instantiated resource
   */
  protected final R createResource(Supplier<R> constructor, CanonicalType profile) {
    val r = this.setIdTo(constructor.get());
    val meta = new Meta().setProfile(List.of(profile));
    r.setMeta(meta);
    return r;
  }
}
