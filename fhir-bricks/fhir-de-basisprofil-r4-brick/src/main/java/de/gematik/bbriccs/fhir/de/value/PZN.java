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

package de.gematik.bbriccs.fhir.de.value;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.coding.WithChecksum;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

/** <a href="https://de.wikipedia.org/wiki/Pharmazentralnummer">Pharmazentralnummer</a> */
@EqualsAndHashCode(callSuper = true)
public class PZN extends SemanticValue<String, DeBasisProfilCodeSystem> implements WithChecksum {
  private static final Pattern PZN_PATTERN = Pattern.compile("^\\d{8}$");

  private PZN(String value) {
    super(DeBasisProfilCodeSystem.PZN, value);
  }

  @Override
  public boolean isValid() {
    val pzn = getValue();
    if (pzn == null) return false;
    val matcher = PZN_PATTERN.matcher(pzn);
    if (!matcher.matches()) return false;

    val calcChecksum = calcChecksum(pzn);
    if (calcChecksum == 10) return false;
    return getChecksum() == calcChecksum;
  }

  @Override
  public int getChecksum() {
    val value = getValue();
    return Character.getNumericValue(value.charAt(value.length() - 1));
  }

  public Coding asCoding() {
    return this.getSystem().asCoding(this.getValue());
  }

  /**
   * With this method, the PZN is wrapped up as a CodeableConcept with the given drugName
   *
   * @param drugName corresponding to the PZN
   * @return CodeableConcept with PZN and drug drugName
   */
  public CodeableConcept asNamedCodeable(String drugName) {
    val codeable = new CodeableConcept(asCoding());
    codeable.setText(drugName);
    return codeable;
  }

  /**
   * With this method, the PZN is wrapped up as a CodeableConcept with a randomly generated name
   *
   * @return CodeableConcept with PZN and a random drug name
   */
  public CodeableConcept asNamedCodeable() {
    val faker = FakerBrick.getGerman();
    return asNamedCodeable(faker.medical().medicineName());
  }

  public static PZN from(String value) {
    return new PZN(value);
  }

  public static PZN random() {
    val faker = FakerBrick.getGerman();
    String numbers;
    int checkNum;
    do {
      numbers = faker.regexify("[0-9]{7}");
      checkNum = calcChecksum(numbers);
    } while (checkNum == 10);
    val value = format("{0}{1}", numbers, checkNum);
    return from(value);
  }

  protected static int calcChecksum(String pzn) {
    int sum = 0;
    for (int i = 0; i < 7; i++) {
      val value = Character.getNumericValue(pzn.charAt(i)) * (i + 1);
      sum += value;
    }
    return sum % 11;
  }
}
