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

package de.gematik.bbriccs.fhir.fuzzing.impl;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.*;
import de.gematik.bbriccs.fhir.fuzzing.PrimitiveType;
import de.gematik.bbriccs.fhir.fuzzing.exceptions.FuzzerException;
import de.gematik.bbriccs.fhir.fuzzing.impl.log.FuzzingLogEntry;
import java.util.*;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Getter
@Slf4j
public class FuzzingContextImpl implements FuzzingContext {

  private final Randomness randomness;

  private final Map<
          Class<? extends Resource>, List<FhirResourceMutatorProvider<? extends Resource>>>
      resourceFuzzer;
  private final Map<Class<? extends Type>, List<FhirTypeMutatorProvider<? extends Type>>>
      typeFuzzer;

  private final Map<PrimitiveType<?>, List<PrimitiveMutatorProvider<?>>> primitiveFuzzer;

  public FuzzingContextImpl(Randomness randomness) {
    this.randomness = randomness;
    this.resourceFuzzer = new HashMap<>();
    this.typeFuzzer = new HashMap<>();
    this.primitiveFuzzer = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> List<FuzzingLogEntry> startFuzzingSession(R resource) {
    val rClass = (Class<R>) resource.getClass();
    val rClassFuzzers = this.getAllResourceFuzzersFor(rClass);

    if (rClassFuzzers.isEmpty()) {
      throw new FuzzerException(
          format("Unable to start Fuzzing because not Fuzzer found for {0}", resource.getClass()));
    }

    return rClassFuzzers.stream()
        .map(FhirResourceMutatorProvider::getMutators)
        .flatMap(
            mutators -> {
              val mutatorAmount = randomness.source().nextInt(1, mutators.size() + 1);
              Collections.shuffle(mutators);
              return IntStream.range(0, mutatorAmount).mapToObj(mutators::get);
            })
        .map(
            mutator -> {
              try {
                return mutator.apply(this, resource);
              } catch (Throwable throwable) {
                return FuzzingLogEntry.error(throwable);
              }
            })
        .toList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> FuzzingLogEntry fuzzChild(String message, R resource) {
    log.trace("Fuzz Child Resource {}: {}", resource.getClass().getSimpleName(), message);
    val entries = this.callResourceFuzzersFor((Class<R>) resource.getClass(), resource);
    return FuzzingLogEntry.parent(message, entries);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> FuzzingLogEntry fuzzChildResources(
      String message, List<R> resources) {
    log.trace("Fuzz Child {} Resources: {}", resources.size(), message);
    val entries =
        resources.stream()
            .flatMap(
                resource ->
                    this.callResourceFuzzersFor((Class<R>) resource.getClass(), resource).stream())
            .toList();
    return FuzzingLogEntry.parent(message, entries);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Type> FuzzingLogEntry fuzzChild(String message, T type) {
    if (type == null) {
      return FuzzingLogEntry.noop(format("{0} but given type is null", message));
    }
    log.trace("Fuzz Child Type {}: {}", type.getClass().getSimpleName(), message);

    val entries = this.callTypeFuzzersFor((Class<T>) type.getClass(), type);
    return FuzzingLogEntry.parent(message, entries);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Type> FuzzingLogEntry fuzzChildTypes(String message, List<T> types) {
    log.trace("Fuzz {} Child Types: {}", types.size(), message);
    val entries =
        types.stream()
            .flatMap(type -> this.callTypeFuzzersFor((Class<T>) type.getClass(), type).stream())
            .toList();
    return FuzzingLogEntry.parent(message, entries);
  }

  @Override
  public <R extends Resource> FuzzingLogEntry fuzzIdElement(
      Class<? extends Resource> parentClass, R parent) {
    if (parent == null) {
      return FuzzingLogEntry.noop(
          format(
              "do not fuzz ID-Element for {0} because the object is null",
              parentClass.getSimpleName()));
    }
    return fuzzIdElement(IdType.class, parent.getIdElement());
  }

  @Override
  public <T extends Type> FuzzingLogEntry fuzzIdElement(Class<T> parentClass, T parent) {
    if (parent == null) {
      return FuzzingLogEntry.noop(
          format(
              "do not fuzz ID-Element for {0} because the object is null",
              parentClass.getSimpleName()));
    }

    if (this.randomness.idDice().toss()) {
      log.trace("Fuzz ID-Element of {}", parentClass.getSimpleName());
      val childLogEntries = new LinkedList<FuzzingLogEntry>();

      if (!parent.hasIdElement()) {
        val value = this.randomness.id();
        val idElement = this.randomness.fhir().createType(StringType.class);

        idElement.setValue(value);
        parent.setIdElement(idElement);
        childLogEntries.add(FuzzingLogEntry.operation("Add new StringType for ID-Element"));
      }
      val idElement = parent.getIdElement();
      childLogEntries.add(this.fuzzChild(parentClass, idElement));
      return FuzzingLogEntry.parent(
          format("Fuzz ID-Element for {0}", parentClass.getSimpleName()), childLogEntries);
    }
    return FuzzingLogEntry.noop(
        format("do not fuzz ID-Element for {0}", parentClass.getSimpleName()));
  }

  @Override
  public <P> PrimitiveTypeFuzzingResponse<P> fuzzPrimitiveType(
      String message, PrimitiveType<P> pType, P value) {
    log.trace("Fuzz Primitive Type {}: {}", pType, message);
    val mutators =
        getAllPrimitiveFuzzersFor(pType).stream()
            .flatMap(
                f -> this.randomness.mutatorDice().chooseRandomElements(f.getMutators()).stream())
            .toList();

    val entries = new LinkedList<FuzzingLogEntry>();
    for (val m : mutators) {
      val response = m.apply(this, value);
      value = response.getFuzzedValue();
      entries.add(response.getLogEntry());
    }
    val fullLog = FuzzingLogEntry.parent(message, entries);
    return PrimitiveTypeFuzzingResponse.response(value, fullLog);
  }

  @Override
  public Randomness randomness() {
    return this.randomness;
  }

  private <T extends Type> List<FuzzingLogEntry> callTypeFuzzersFor(Class<T> tClass, T typeValue) {
    val mutators =
        this.getAllTypeFuzzersFor(tClass).stream()
            .flatMap(
                tf -> this.randomness.mutatorDice().chooseRandomElements(tf.getMutators()).stream())
            .toList();

    if (mutators.isEmpty()) {
      return List.of(
          FuzzingLogEntry.noop(
              format(
                  "no TypeFuzzer found or chosen for {0} with ID ''{1}''",
                  tClass.getSimpleName(), typeValue.getId())));
    }

    return mutators.stream()
        .map(
            mutator -> {
              try {
                return mutator.apply(this, typeValue);
              } catch (Throwable throwable) {
                log.warn(
                    "Caught throwable {} while applying mutator",
                    throwable.getClass().getSimpleName());
                return FuzzingLogEntry.error(throwable);
              }
            })
        .toList();
  }

  protected <T extends Type> Optional<FhirTypeMutatorProvider<T>> getTypeFuzzerFor(
      Class<T> tClass) {
    return this.randomness.chooseRandomly(getAllTypeFuzzersFor(tClass));
  }

  @SuppressWarnings("unchecked")
  private <T extends Type> List<FhirTypeMutatorProvider<T>> getAllTypeFuzzersFor(Class<T> tClass) {
    val typeFuzzers =
        new ArrayList<>(
            typeFuzzer.computeIfAbsent(tClass, k -> new LinkedList<>()).stream()
                .map(x -> (FhirTypeMutatorProvider<T>) x)
                .toList());

    // find also matching fuzzers for the superClass
    val superClass = (Class<? extends Type>) tClass.getSuperclass();
    val superMatches =
        typeFuzzer.computeIfAbsent(superClass, k -> new LinkedList<>()).stream()
            .map(x -> (FhirTypeMutatorProvider<T>) x)
            .toList();
    typeFuzzers.addAll(superMatches);

    if (typeFuzzers.isEmpty()) {
      log.warn("No Fuzzers found for requested Type {}", tClass.getSimpleName());
    }

    return typeFuzzers;
  }

  protected <R extends Resource> Optional<FhirResourceMutatorProvider<R>> getResourceFuzzerFor(
      Class<R> rClass) {
    return this.randomness.chooseRandomly(getAllResourceFuzzersFor(rClass));
  }

  private <R extends Resource> List<FuzzingLogEntry> callResourceFuzzersFor(
      Class<R> rClass, R resource) {
    return getAllResourceFuzzersFor(rClass).stream()
        .flatMap(
            rf -> this.randomness.mutatorDice().chooseRandomElements(rf.getMutators()).stream())
        .map(mutator -> mutator.apply(this, resource))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private <R extends Resource> List<FhirResourceMutatorProvider<R>> getAllResourceFuzzersFor(
      Class<R> rClass) {
    val resourceFuzzers =
        new ArrayList<>(
            resourceFuzzer.computeIfAbsent(rClass, k -> new LinkedList<>()).stream()
                .map(x -> (FhirResourceMutatorProvider<R>) x)
                .toList());

    // find also matching fuzzers for the superClass
    val superClass = (Class<? extends Resource>) rClass.getSuperclass();
    val superMatches =
        resourceFuzzer.computeIfAbsent(superClass, k -> new LinkedList<>()).stream()
            .map(x -> (FhirResourceMutatorProvider<R>) x)
            .toList();
    resourceFuzzers.addAll(superMatches);

    if (resourceFuzzers.isEmpty()) {
      log.warn("No Fuzzers found for requested Resource {}", rClass.getSimpleName());
    }

    return resourceFuzzers;
  }

  <P> Optional<PrimitiveMutatorProvider<P>> getPrimitiveFuzzerFor(PrimitiveType<P> pType) {
    return this.randomness.chooseRandomly(getAllPrimitiveFuzzersFor(pType));
  }

  @SuppressWarnings("unchecked")
  private <P> List<PrimitiveMutatorProvider<P>> getAllPrimitiveFuzzersFor(PrimitiveType<P> pType) {
    val fuzzers =
        primitiveFuzzer.computeIfAbsent(pType, k -> new LinkedList<>()).stream()
            .map(x -> (PrimitiveMutatorProvider<P>) x)
            .toList();

    if (fuzzers.isEmpty()) {
      log.warn("No Fuzzers found for requested primitive type {}", pType);
    }

    return fuzzers;
  }
}
