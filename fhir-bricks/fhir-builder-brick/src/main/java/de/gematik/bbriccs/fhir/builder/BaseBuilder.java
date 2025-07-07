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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.coding.FromValueSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.val;
import org.assertj.core.util.Strings;
import org.hl7.fhir.r4.model.Base;

public abstract class BaseBuilder<R extends Base, B extends BaseBuilder<R, B>> {

  private String resourceId;

  public final B setId(String resourceId) {
    this.resourceId = resourceId;
    return self();
  }

  protected abstract R setIdTo(R base);

  /**
   * The resource ID is always required but does not necessarily need to be provided by the user. In
   * case the user didn't provide one, a random UUID is generated automatically.
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
   * This method is required to allow common methods to return the concrete builder and enable all
   * builders to have this method
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

  protected final void checkRequiredExactlyOneOf(String errorMsg, Object... objects) {
    val nonNull = Arrays.stream(objects).filter(Objects::nonNull).count();

    if (nonNull == 0) {
      val prefixedErrorMsg = format("Missing required property: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
    if (nonNull > 1) {
      val prefixedErrorMsg =
          format(
              "Too many properties, required exactly one of but received {0}: {1}",
              nonNull, errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  @SafeVarargs
  protected final <T extends FromValueSet> void checkValueSet(T obj, T... oneOf) {
    checkValueSet(obj, List.of(oneOf));
  }

  protected final <T extends FromValueSet> void checkValueSet(T obj, List<T> oneOf) {
    checkRequired(obj, "Valueset choice");
    checkRequiredList(oneOf, 1, format("Expected valueset choice"));
    if (!oneOf.contains(obj)) {
      val errorMsg =
          format(
              "Given valueset {0} is not in the list of expected choices: ''{1}''",
              obj, Strings.join(oneOf).with(", "));
      throw new BuilderException(errorMsg);
    }
  }

  public abstract R build();
}
