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

package de.gematik.bbriccs.fhir.builder;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.ProfileStructureDefinition;
import de.gematik.bbriccs.fhir.coding.version.ProfileVersion;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;

public abstract class ResourceBuilder<R extends Resource, B extends ResourceBuilder<R, B>> {

  private String resourceId;

  public final B setResourceId(String resourceId) {
    this.resourceId = resourceId;
    return self();
  }

  protected final R setResourceIdTo(R resource) {
    resource.setId(this.getResourceId());
    return resource;
  }

  protected final R createResource(Supplier<R> constructor, ProfileStructureDefinition<?> profile) {
    return createResource(constructor, profile.asCanonicalType());
  }

  protected final <V extends ProfileVersion> R createResource(
      Supplier<R> constructor, ProfileStructureDefinition<V> profile, V version) {
    val profileCanonical = profile.asCanonicalType(version);
    return createResource(constructor, profileCanonical);
  }

  /**
   * This method will use the given construction supplier to create the resource, set a resource-id
   * and the given canonical type as the profile into the meta-information of the resource
   *
   * @param constructor to instantiate the resource object
   * @param profile to use for this resource in the meta-information
   * @return the instantiated resource
   */
  protected final R createResource(Supplier<R> constructor, CanonicalType profile) {
    val r = this.setResourceIdTo(constructor.get());
    val meta = new Meta().setProfile(List.of(profile));
    r.setMeta(meta);
    return r;
  }

  /**
   * The resource ID is always required but does not necessarily need to be provided by the user. In
   * case the user didn't provide one, generate a random UUID
   *
   * @return Resource ID provided by user or a randomly generated one if no ID was provided
   */
  protected final String getResourceId() {
    if (this.resourceId == null) {
      this.resourceId = UUID.randomUUID().toString();
    }
    return resourceId;
  }

  /**
   * This method is required to allow {@link #setResourceId(String)} to return the concrete builder
   * and enable all builders to have this method
   *
   * @return the self-builder
   */
  @SuppressWarnings("unchecked")
  protected final B self() {
    return (B) this;
  }

  /**
   * This method works similar to Objects.requireNonNull, but instead of throwing a
   * NullPointerException, this one will throw BuilderException with a message supplied by the
   * errorMsgSupplier
   *
   * @param obj is the object which will be checked for Null
   * @param errorMsg will be shown in the BuilderException in case of an Error
   * @param <T> is the generic type of obj
   */
  protected final <T> void checkRequired(T obj, String errorMsg) {
    if (obj == null) {
      val prefixedErrorMsg = format("Missing required property: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  protected final <T> void checkRequiredList(List<T> list, int min, String errorMsg) {
    checkRequired(list, errorMsg);
    if (min <= 0) {
      throw new IllegalArgumentException(
          format("Minimum amount must be >= 1 but was given {0}", min));
    }

    if (list.size() < min) {
      val prefixedErrorMsg =
          format("List (size {0}) missing required elements: {1}", list.size(), min);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  public abstract R build();
}
