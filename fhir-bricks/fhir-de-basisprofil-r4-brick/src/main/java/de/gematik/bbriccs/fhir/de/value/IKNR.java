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

package de.gematik.bbriccs.fhir.de.value;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.fhir.coding.WithChecksum;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidSystemException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;

/** <a href="https://de.wikipedia.org/wiki/Institutionskennzeichen">Institutionskennzeichen</a> */
@EqualsAndHashCode(callSuper = true)
public class IKNR extends SemanticValue<String, DeBasisProfilNamingSystem> implements WithChecksum {
  private static final Pattern IKNR_PATTERN = Pattern.compile("^\\d{9}$");

  private IKNR(DeBasisProfilNamingSystem namingSystem, String iknr) {
    super(namingSystem, iknr);
  }

  @Override
  public boolean isValid() {
    val iknr = getValue();
    if (StringUtils.isBlank(iknr)) return false;

    val matcher = IKNR_PATTERN.matcher(iknr);
    if (!matcher.matches()) return false;
    val calcChecksum = calcChecksum(iknr);
    return getChecksum() == calcChecksum;
  }

  @Override
  public int getChecksum() {
    val value = getValue();
    return Character.getNumericValue(value.charAt(value.length() - 1));
  }

  public static IKNR from(Identifier identifier) {
    return Stream.of(DeBasisProfilNamingSystem.IKNR, DeBasisProfilNamingSystem.IKNR_SID)
        .filter(system -> system.matches(identifier))
        .map(system -> new IKNR(system, identifier.getValue()))
        .findFirst()
        .orElseThrow(() -> new InvalidSystemException(IKNR.class, identifier.getSystem()));
  }

  /**
   * Creating an IKNR with the default NamingSystem
   *
   * @param value of the IKNR
   * @return an IKNR object carrying the semantic value and the default Naming System for the IKNR
   */
  public static IKNR asDefaultIknr(String value) {
    return asSidIknr(value);
  }

  public static IKNR asArgeIknr(String value) {
    return new IKNR(DeBasisProfilNamingSystem.IKNR, value);
  }

  public static IKNR asSidIknr(String value) {
    return new IKNR(DeBasisProfilNamingSystem.IKNR_SID, value);
  }

  public static IKNR random() {
    val faker = FakerBrick.getGerman();
    if (faker.bool().bool()) return randomArgeIknr();
    else return randomSidIknr();
  }

  public static IKNR randomArgeIknr() {
    return asArgeIknr(randomStringValue());
  }

  public static IKNR randomSidIknr() {
    return asSidIknr(randomStringValue());
  }

  public static String randomStringValue() {
    val faker = FakerBrick.getGerman();
    val numbers = faker.regexify("\\d{8}");
    val checksum = calcChecksum(numbers);
    return format("{0}{1}", numbers, checksum);
  }

  private static int calcChecksum(String number) {
    var sum = 0;
    for (var i = 7; i >= 2; i--) {
      var value = Character.getNumericValue(number.charAt(i));
      if (i % 2 == 0) value *= 2;
      sum += WithChecksum.crossSum(value);
    }
    return sum % 10;
  }
}
