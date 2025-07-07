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

package de.gematik.bbriccs.fhir.fuzzing;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public interface Randomness {

  FhirRandomness fhir();

  ProbabilityDice idDice();

  ProbabilityDice mutatorDice();

  ProbabilityDice childResourceDice();

  <T> T chooseRandomElement(List<T> elements);

  <T> Optional<T> chooseRandomly(List<T> elements);

  <E extends Enum<?>> E chooseRandomFromEnum(Class<E> enumeration);

  <E extends Enum<?>> E chooseRandomFromEnum(Class<E> enumeration, E exclude);

  <E extends Enum<?>> E chooseRandomFromEnum(Class<E> enumeration, List<E> exclude);

  Random source();

  String url();

  String url(Object... path);

  String uuid();

  default String id() {
    if (source().nextBoolean()) {
      return uuid();
    } else {
      return regexify("[A-Za-z0-9-_$#%()]{5,50}");
    }
  }

  String regexify(String regex);

  String version();

  Date date();

  LocalTime time();
}
