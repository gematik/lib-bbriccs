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
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/**
 * Represents a Krankenversichertennummer (KVNR), a German health insurance number. This class
 * provides methods to generate, validate, and manipulate KVNRs.
 *
 * <p>KVNRs are used to uniquely identify individuals in the German health insurance system. They
 * consist of a leading capital letter, followed by 8 digits, and a check number.
 *
 * <p>For more information, see <a
 * href="https://de.wikipedia.org/wiki/Krankenversichertennummer">Krankenversichertennummer</a>.
 *
 * <p>This class supports both GKV (statutory health insurance) and PKV (private health insurance)
 * systems, and provides methods to generate random KVNRs for both systems.
 *
 * <p>Example usage:
 *
 * <pre>
 *     KVNR kvnr = KVNR.random();
 *     boolean isValid = kvnr.isValid();
 * </pre>
 *
 * <p>Note: This class implements the {@link WithChecksum} interface to provide checksum validation.
 *
 * @see DeBasisProfilNamingSystem
 * @see InsuranceTypeDe
 * @see WithChecksum
 */
@EqualsAndHashCode(callSuper = true)
public class KVNR extends SemanticValue<String, DeBasisProfilNamingSystem> implements WithChecksum {

  private static final List<DeBasisProfilNamingSystem> KVNR_SYSTEMS =
      List.of(
          DeBasisProfilNamingSystem.KVID_GKV_SID,
          DeBasisProfilNamingSystem.KVID_PKV_SID,
          DeBasisProfilNamingSystem.KVID);
  private static final Pattern KVNR_PATTERN = Pattern.compile("^([A-Z])(\\d{8})(\\d)$");

  private KVNR(DeBasisProfilNamingSystem namingSystem, String value) {
    super(namingSystem, value);
  }

  public InsuranceTypeDe getInsuranceType() {
    return switch (this.getSystem()) {
      case KVID, KVID_GKV_SID -> InsuranceTypeDe.GKV;
      case KVID_PKV_SID -> InsuranceTypeDe.PKV;
      default -> throw new InvalidSystemException(this.getClass(), this.getSystem());
    };
  }

  public boolean isGkv() {
    return this.getSystem()
        .matches(DeBasisProfilNamingSystem.KVID_GKV_SID, DeBasisProfilNamingSystem.KVID);
  }

  public boolean isPkv() {
    return this.getSystem().matches(DeBasisProfilNamingSystem.KVID_PKV_SID);
  }

  public Reference asReference(boolean withCoding) {
    return asReference(this.getSystem(), withCoding);
  }

  public Reference asReference(DeBasisProfilNamingSystem system, boolean withCoding) {
    val ref = new Reference();
    ref.setIdentifier(asIdentifier(system, withCoding));
    return ref;
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

    if (withCoding) {
      identifier.getType().addCoding(this.getInsuranceType().asCoding());
    }
    return identifier;
  }

  @Override
  public boolean isValid() {
    val kvnr = getValue();
    if (kvnr == null) {
      return false;
    }

    val matcher = KVNR_PATTERN.matcher(kvnr);
    if (!matcher.matches()) {
      return false;
    }

    val calculated = calculateCheckNumber(matcher.group(0).charAt(0), matcher.group(2));

    return calculated == getChecksum();
  }

  @Override
  public int getChecksum() {
    val value = getValue();
    return Character.getNumericValue(value.charAt(value.length() - 1));
  }

  public static KVNR random() {
    val faker = FakerBrick.getGerman();
    if (faker.bool().bool()) {
      return randomGkv();
    } else {
      return randomPkv();
    }
  }

  public static KVNR randomPkv() {
    return asPkv(randomStringValue());
  }

  public static KVNR randomGkv() {
    return asGkv(randomStringValue());
  }

  public static KVNR asPkv(String value) {
    return from(DeBasisProfilNamingSystem.KVID_PKV_SID, value);
  }

  public static KVNR asGkv(String value) {
    return from(DeBasisProfilNamingSystem.KVID_GKV_SID, value);
  }

  public static KVNR from(String value) {
    return from(DeBasisProfilNamingSystem.KVID, value);
  }

  public static KVNR from(DeBasisProfilNamingSystem namingSystem, String value) {
    return new KVNR(namingSystem, value);
  }

  public static KVNR from(Identifier identifier) {
    return extractFrom(identifier)
        .orElseThrow(() -> new InvalidSystemException(KVNR.class, identifier.getSystem()));
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
    val checkNum = calculateCheckNumber(capLetter, numbers);
    return format("{0}{1}{2}", capLetter, numbers, checkNum);
  }

  /**
   * Calculates the check number for a given "chunked" KVNR (Krankenversichertennummer) without the
   * check number. The check number is placed as the last digit on the "full" KVNR.
   *
   * @param capLetter the leading capital letter [A-Z]
   * @param numbers the 8 random digits
   * @return the calculated check number
   */
  private static int calculateCheckNumber(char capLetter, String numbers) {
    val letterValue = String.format("%02d", capLetter - 64);
    val rawNumber = format("{0}{1}", letterValue, numbers);

    val idx = new AtomicInteger();
    val sum = new AtomicInteger();
    rawNumber
        .chars()
        .map(asciiValue -> asciiValue - 48)
        .forEach(
            value -> {
              if (idx.getAndIncrement() % 2 == 1) {
                value *= 2;
              }
              if (value > 9) {
                value -= 9;
              }
              sum.addAndGet(value);
            });
    return sum.get() % 10;
  }
}
