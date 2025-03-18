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

package de.gematik.bbriccs.fhir.fuzzing.impl.rnd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.fuzzing.FhirRandomness;
import de.gematik.bbriccs.fhir.fuzzing.ProbabilityDice;
import de.gematik.bbriccs.fhir.fuzzing.Randomness;
import de.gematik.bbriccs.fhir.fuzzing.exceptions.FuzzerException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import lombok.val;

public class RandomnessImpl implements Randomness {

  private final Random rnd;

  private final FakerBrick faker;

  /** This dice is specifically for manipulating IdTypes */
  private final ProbabilityDice pdIdTypes;

  private final ProbabilityDice pdMutator;
  private final ProbabilityDice pdChildResources;
  private final FhirRandomness fhir;

  public RandomnessImpl(Random rnd, ProbabilityDice pdMutator, ProbabilityDice pdChildResources) {
    this.rnd = rnd;
    this.pdIdTypes = new ProbabilityDiceImpl(rnd, 0.3); // TODO: resolve me later!
    this.pdMutator = pdMutator;
    this.pdChildResources = pdChildResources;
    this.faker = new FakerBrick(new Locale("de"), rnd);
    this.fhir = new FhirRandomnessImpl(this);
  }

  @Override
  public FhirRandomness fhir() {
    return this.fhir;
  }

  @Override
  public ProbabilityDice idDice() {
    return this.pdIdTypes;
  }

  @Override
  public ProbabilityDice mutatorDice() {
    return this.pdMutator;
  }

  @Override
  public ProbabilityDice childResourceDice() {
    return this.pdChildResources;
  }

  @Override
  public <T> T chooseRandomElement(List<T> elements) {
    return chooseRandomly(elements)
        .orElseThrow(
            () -> new FuzzerException("Impossible to pick random element from empty list"));
  }

  @Override
  public <T> Optional<T> chooseRandomly(List<T> elements) {
    if (elements.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(faker.randomElement(elements));
  }

  @Override
  public <E extends Enum<?>> E chooseRandomFromEnum(Class<E> enumeration) {
    return this.faker.randomEnum(enumeration);
  }

  @Override
  public <E extends Enum<?>> E chooseRandomFromEnum(Class<E> enumeration, E exclude) {
    if (exclude != null) return this.faker.randomEnum(enumeration, exclude);
    else return this.faker.randomEnum(enumeration);
  }

  @Override
  public <E extends Enum<?>> E chooseRandomFromEnum(Class<E> enumeration, List<E> exclude) {
    return this.faker.randomEnum(enumeration, exclude);
  }

  @Override
  public Random source() {
    return this.rnd;
  }

  @Override
  public String url() {
    val scheme = chooseRandomElement(List.of("http://", "https://"));
    return scheme + faker.internet().url();
  }

  @Override
  public String url(Object... path) {
    var ub = new StringBuilder(url());
    Arrays.stream(path).forEach(p -> ub.append(format("/{0}", p)));
    return ub.toString();
  }

  @Override
  public String uuid() {
    return faker.internet().uuid();
  }

  @Override
  public String regexify(String regex) {
    return faker.regexify(regex);
  }

  @Override
  public String version() {
    return faker.app().version();
  }

  @Override
  public Date date() {
    val maxDate = new GregorianCalendar(9999, Calendar.DECEMBER, 31).getTime();
    return faker.date().between(new Date(0L), maxDate);
  }

  @Override
  public LocalTime time() {
    return LocalDateTime.ofInstant(date().toInstant(), ZoneId.systemDefault()).toLocalTime();
  }
}
