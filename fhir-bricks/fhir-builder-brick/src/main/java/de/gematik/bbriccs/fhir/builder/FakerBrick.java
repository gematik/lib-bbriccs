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

package de.gematik.bbriccs.fhir.builder;

import static java.text.MessageFormat.format;

import com.github.javafaker.Faker;
import de.gematik.bbriccs.fhir.builder.exceptions.FakerException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;
import lombok.experimental.Delegate;
import lombok.val;

public class FakerBrick {

  private static final Map<Locale, Faker> FAKER_CACHE = new HashMap<>();

  private static final DecimalFormat PRICE_FORMAT =
      new DecimalFormat("##.##", new DecimalFormatSymbols(Locale.US));

  @Delegate private final Faker faker;

  private FakerBrick(Locale locale) {
    this.faker = FAKER_CACHE.computeIfAbsent(locale, Faker::new);
  }

  /**
   * This constructor builds a FakerBrick with dedicated Faker (without caching) and is only
   * intended to be used in a long-living context
   *
   * @param locale to be used for the Faker
   * @param rnd source for randomness for the Faker
   */
  public FakerBrick(Locale locale, Random rnd) {
    this.faker = new Faker(locale, rnd);
  }

  /**
   * This static access provides a FakerBrick for german localization with a cached Faker to improve
   * performance when accessing sporadically just a couple of methods in a short-living context
   *
   * @return a FakerBrick for german localization with a cached faker
   */
  public static FakerBrick getGerman() {
    return getLocalized(new Locale("de"));
  }

  /**
   * This static access provides a FakerBrick for custom localization with a cached Faker to improve
   * performance when accessing sporadically just a couple of methods in a short living context
   *
   * @return a FakerBrick for custom localization with a cached faker
   */
  public static FakerBrick getLocalized(Locale locale) {
    return new FakerBrick(locale);
  }

  public double price() {
    return price(0, 300);
  }

  public double price(double min, double max) {
    double price = min + (faker.random().nextDouble() * (max - min));
    return Double.parseDouble(PRICE_FORMAT.format(price));
  }

  public String streetName() {
    return faker.address().streetAddress();
  }

  /**
   * Get a random value from a given Enum
   *
   * @param valueSet is the class of the Enum
   * @param <V> is the type of the
   * @return a random choice
   */
  public <V extends Enum<?>> V randomEnum(Class<V> valueSet) {
    return randomEnum(valueSet, List.of());
  }

  public <V extends Enum<?>> V randomEnum(Class<V> valueSet, V exclude) {
    return randomEnum(valueSet, List.of(exclude));
  }

  public <V extends Enum<?>> V randomEnum(Class<V> valueSet, List<V> exclude) {
    val included =
        Arrays.stream(valueSet.getEnumConstants()).filter(ec -> !exclude.contains(ec)).toList();

    if (included.isEmpty()) {
      throw new FakerException(
          format(
              "List of included choices for {0} is empty: probably all possible choices are"
                  + " excluded {1}",
              valueSet.getSimpleName(),
              exclude.stream().map(Enum::name).collect(Collectors.joining(", "))));
    }

    val idx = this.random().nextInt(included.size());
    return included.get(idx);
  }

  @SafeVarargs
  public final <T> T randomElement(T... elements) {
    return randomElement(List.of(elements));
  }

  public final <T> T randomElement(List<T> list) {
    val idx = faker.random().nextInt(0, list.size() - 1);
    return list.get(idx);
  }
}
