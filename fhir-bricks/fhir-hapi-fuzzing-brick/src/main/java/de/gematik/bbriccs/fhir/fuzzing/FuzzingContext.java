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

package de.gematik.bbriccs.fhir.fuzzing;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public interface FuzzingContext {

  /**
   * This is the entry-point to a new Fuzzing-Session. In contrast to {@link #fuzzChild}, starting a
   * new Fuzzing-Session must ensure, even for small probabilities, the fuzzer finds an entry-point.
   *
   * @param resource to be fuzzed
   * @return the fuzzed resource
   * @param <R> concrete type of the resource
   */
  <R extends Resource> List<FuzzingLogEntry> startFuzzingSession(R resource);

  <R extends Resource> FuzzingLogEntry fuzzChild(String message, R resource);

  default <R extends Resource, P> FuzzingLogEntry fuzzChild(Class<P> parent, R resource) {
    val message =
        format(
            "Call ResourceFuzzers for {0} in {1}",
            resource.getClass().getSimpleName(), parent.getSimpleName());
    return this.fuzzChild(message, resource);
  }

  default <R extends Resource, P> FuzzingLogEntry fuzzChildResources(
      Class<P> parent, List<R> resources) {
    val message =
        format(
            "Call ResourceFuzzers for {0} children resources of {1}",
            resources.size(), parent.getSimpleName());
    return this.fuzzChildResources(message, resources);
  }

  <R extends Resource> FuzzingLogEntry fuzzChildResources(String message, List<R> resources);

  default <B extends Bundle> FuzzingLogEntry fuzzChildResources(B parent) {
    val entryResource =
        this.randomness()
            .childResourceDice()
            .chooseRandomElements(
                parent.getEntry().stream().map(Bundle.BundleEntryComponent::getResource).toList());

    if (entryResource.isEmpty()) {
      return FuzzingLogEntry.noop(
          format(
              "Bundle {0} with ID ''{1}'' does not contain any resources",
              parent.getClass().getSimpleName(), parent.getId()));
    }

    val message =
        format(
            "Call ResourceFuzzers for Child-Resources in Bundle with ID ''{0}'':  {1}",
            parent.getId(),
            entryResource.stream()
                .map(r -> r.getResourceType().name())
                .collect(Collectors.joining("/")));
    return this.fuzzChildResources(message, entryResource);
  }

  /**
   * Apply Fuzzing to the given Type
   *
   * @param type to be fuzzed
   * @param <T> generic type-bound of the given Type
   */
  <T extends Type> FuzzingLogEntry fuzzChild(String message, T type);

  default <T extends Type, P> FuzzingLogEntry fuzzChild(Class<P> parent, T type) {
    val pClassName = parent.getSimpleName();
    if (type == null) {
      return FuzzingLogEntry.noop(format("in {0} the given type is null", pClassName));
    }

    val message =
        format("Call TypeFuzzers for {0} in {1}", type.getClass().getSimpleName(), pClassName);
    return this.fuzzChild(message, type);
  }

  default <T extends Type, P> FuzzingLogEntry fuzzChild(
      P parent, BooleanSupplier hasType, Supplier<T> typeSupplier) {
    return fuzzChild(parent, hasType.getAsBoolean(), typeSupplier);
  }

  /**
   * Fuzz a child type-element by retrieving via the given supplier
   *
   * @param parent of the type-element where the child element is taken from
   * @param hasType defines if the parent has already the type-element before applying the supplier
   * @param typeSupplier to retrieve the child type-element
   * @return a FuzzingLogEntry describing the performed fuzzing operations
   * @param <T> binds the concrete type of the child-element
   * @param <P> binds the concrete type of the parent
   */
  default <T extends Type, P> FuzzingLogEntry fuzzChild(
      P parent, boolean hasType, Supplier<T> typeSupplier) {
    T type;
    try {
      type = typeSupplier.get();
    } catch (Throwable throwable) {
      // applying supplier on immutable lists can cause UnsupportedOperationException
      // this results from violation of the LSP (Liskov), when a fhir-object receives a list via
      // List.of()
      return FuzzingLogEntry.error(throwable);
    }

    val tClassName = type.getClass().getSimpleName();
    val pClassName = parent.getClass().getSimpleName();

    val message = format("Call TypeFuzzers for {0} in {1}", tClassName, pClassName);
    val childLog = this.fuzzChild(message, type);

    if (hasType) {
      return childLog;
    } else {
      val addTypeLog = format("Add new Type {0} for {1}", tClassName, pClassName);
      return FuzzingLogEntry.add(addTypeLog, childLog);
    }
  }

  <T extends Type> FuzzingLogEntry fuzzChildTypes(String message, List<T> types);

  default <T extends Type, P> FuzzingLogEntry fuzzChildTypes(Class<P> parent, List<T> types) {
    val pClassName = parent.getSimpleName();
    if (types.isEmpty()) {
      return FuzzingLogEntry.noop("given List of types is empty");
    } else {
      val message =
          format(
              "Call TypeFuzzers on {0} for type(s) {1}",
              pClassName, types.stream().map(Type::fhirType).collect(Collectors.joining("/")));
      return this.fuzzChildTypes(message, types);
    }
  }

  default <T extends Type, P> FuzzingLogEntry fuzzChildTypes(
      Class<P> parent, List<T> types, Supplier<T> addConstruction) {
    if (types.isEmpty()) {
      return this.fuzzChild(parent, false, addConstruction);
    } else {
      return this.fuzzChildTypes(parent, types);
    }
  }

  <R extends Resource> FuzzingLogEntry fuzzIdElement(
      Class<? extends Resource> parentClass, R parent);

  <T extends Type> FuzzingLogEntry fuzzIdElement(Class<T> parentClass, T parent);

  <P> PrimitiveTypeFuzzingResponse<P> fuzzPrimitiveType(
      String message, PrimitiveType<P> pType, P value);

  Randomness randomness();
}
