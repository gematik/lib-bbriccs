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

package de.gematik.bbriccs.fhir.fuzzing.impl.rnd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.fuzzing.ProbabilityDice;
import java.util.*;
import java.util.stream.Collectors;
import lombok.val;

public class ProbabilityDiceImpl implements ProbabilityDice {

  private final Random rnd;
  private final double probability;

  public ProbabilityDiceImpl(Random rnd, double probability) {
    if (probability < 0.0 || probability > 1.0) {
      throw new IllegalArgumentException(
          format("Probability must be in range of 0.0 .. 1.0 but was given {0}", probability));
    }

    this.probability = probability;
    this.rnd = rnd;
  }

  @Override
  public boolean toss() {
    return rnd.nextFloat() <= probability;
  }

  @Override
  public <T> List<T> chooseRandomElements(List<T> elements) {
    val ret = elements.stream().filter(tf -> toss()).collect(Collectors.toList());
    Collections.shuffle(ret, this.rnd);
    return ret;
  }
}
