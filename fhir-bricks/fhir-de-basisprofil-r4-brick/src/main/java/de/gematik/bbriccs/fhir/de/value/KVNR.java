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
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidSystemException;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.val;
import one.util.streamex.StreamEx;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;

/**
 * <a href="https://de.wikipedia.org/wiki/Krankenversichertennummer">Krankenversichertennummer</a>
 */
@EqualsAndHashCode(callSuper = true)
public class KVNR extends SemanticValue<String, DeBasisProfilNamingSystem> implements WithChecksum {

  private static final List<DeBasisProfilNamingSystem> KVNR_SYSTEMS =
      List.of(
          DeBasisProfilNamingSystem.SID_KVID_GKV,
          DeBasisProfilNamingSystem.SID_KVID_PKV,
          DeBasisProfilNamingSystem.KVID);
  private static final Pattern KVNR_PATTERN = Pattern.compile("^([A-Z])(\\d{8})(\\d)$");

  private KVNR(DeBasisProfilNamingSystem namingSystem, String value) {
    super(namingSystem, value);
  }

  public InsuranceTypeDe getInsuranceType() {
    return switch (this.getSystem()) {
      case KVID, SID_KVID_GKV -> InsuranceTypeDe.GKV;
      case SID_KVID_PKV -> InsuranceTypeDe.PKV;
      default -> throw new InvalidSystemException(this.getClass(), this.getSystem());
    };
  }

  public boolean isGkv() {
    return this.getSystem()
        .matches(DeBasisProfilNamingSystem.SID_KVID_GKV, DeBasisProfilNamingSystem.KVID);
  }

  public boolean isPkv() {
    return this.getSystem().matches(DeBasisProfilNamingSystem.SID_KVID_PKV);
  }

  @Override
  public Identifier asIdentifier() {
    return asIdentifier(true);
  }

  public Identifier asIdentifier(boolean withCoding) {
    return this.asIdentifier(this.getSystem(), withCoding);
  }

  @Override
  public Identifier asIdentifier(DeBasisProfilNamingSystem system) {
    return asIdentifier(system, true);
  }

  public Identifier asIdentifier(DeBasisProfilNamingSystem system, boolean withCoding) {
    val identifier = super.asIdentifier(system);

    if (withCoding) identifier.getType().addCoding(this.getInsuranceType().asCoding());
    return identifier;
  }

  @Override
  public boolean isValid() {
    val kvnr = getValue();
    if (kvnr == null) return false;

    val matcher = KVNR_PATTERN.matcher(kvnr);
    if (!matcher.matches()) return false;

    val calculated = getCalculateCheckNumber(matcher.group(0).charAt(0), matcher.group(2));

    return calculated == getChecksum();
  }

  @Override
  public int getChecksum() {
    val value = getValue();
    return Character.getNumericValue(value.charAt(value.length() - 1));
  }

  public static KVNR random() {
    val faker = FakerBrick.getGerman();
    if (faker.bool().bool()) return randomGkv();
    else return randomPkv();
  }

  public static KVNR randomPkv() {
    return forPkv(randomStringValue());
  }

  public static KVNR randomGkv() {
    return forGkv(randomStringValue());
  }

  public static KVNR forPkv(String value) {
    return from(DeBasisProfilNamingSystem.SID_KVID_PKV, value);
  }

  public static KVNR forGkv(String value) {
    return from(DeBasisProfilNamingSystem.SID_KVID_GKV, value);
  }

  public static KVNR from(String value) {
    return from(DeBasisProfilNamingSystem.KVID, value);
  }

  public static KVNR from(DeBasisProfilNamingSystem namingSystem, String value) {
    return new KVNR(namingSystem, value);
  }

  public static Optional<KVNR> extractFrom(Identifier identifier) {
    return extractFrom(List.of(identifier));
  }

  public static Optional<KVNR> extractFrom(List<Identifier> identifiers) {
    return StreamEx.of(identifiers)
        .cross(KVNR_SYSTEMS)
        .filter(entry -> entry.getValue().matches(entry.getKey()))
        .map(
            entry -> {
              val system = entry.getValue();
              val identifierValue = entry.getKey().getValue();
              return new KVNR(system, identifierValue);
            })
        .findFirst();
  }

  public static <P extends Resource> KVNR extractFromOrThrow(
      Class<P> parent, Identifier identifier) {
    return extractFromOrThrow(parent, List.of(identifier));
  }

  public static <P extends Resource> KVNR extractFromOrThrow(
      Class<P> parent, List<Identifier> identifiers) {
    return extractFrom(identifiers).orElseThrow(() -> new MissingFieldException(parent, "KVNR"));
  }

  public static String randomStringValue() {
    val faker = FakerBrick.getGerman();
    val capLetter = faker.regexify("[A-Z]{1}").charAt(0);
    val numbers = faker.regexify("[0-9]{8}");
    val checkNum = getCalculateCheckNumber(capLetter, numbers);
    return format("{0}{1}{2}", capLetter, numbers, checkNum);
  }

  /**
   * get the chunked KVNR without the check number and calculate the check number
   *
   * @param capLetter is the leading capital letter [A-Z]
   * @param numbers 8 random digits
   * @return the calculated check number
   */
  private static int getCalculateCheckNumber(char capLetter, String numbers) {
    val letterValue = String.format("%02d", capLetter - 64);
    val rawNumber = format("{0}{1}", letterValue, numbers);

    val idx = new AtomicInteger();
    var sum = new AtomicInteger();
    rawNumber
        .chars()
        .map(asciiValue -> asciiValue - 48)
        .forEach(
            value -> {
              if (idx.getAndIncrement() % 2 == 1) value *= 2;
              if (value > 9) value -= 9;
              sum.addAndGet(value);
            });
    return sum.get() % 10;
  }
}
