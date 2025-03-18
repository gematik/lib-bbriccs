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

package de.gematik.bbriccs.fhir.de.builder;

import com.google.common.base.Strings;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.HumanName;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HumanNameBuilder {

  private final HumanName.NameUse use;
  private String family;
  private String given;
  private String prefix;

  public static HumanNameBuilder official() {
    return new HumanNameBuilder(HumanName.NameUse.OFFICIAL);
  }

  public HumanNameBuilder given(String given) {
    this.given = given;
    return this;
  }

  public HumanNameBuilder family(String family) {
    this.family = family;
    return this;
  }

  public HumanNameBuilder prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  /**
   * Build the human name for the "old profiles" without any additional extensions
   *
   * @return HumanName according to older profiles
   */
  public HumanName buildSimple() {
    val name = new HumanName();
    name.setUse(this.use);
    name.addGiven(given).setFamily(family);

    if (StringUtils.isNotBlank(prefix)) {
      name.addPrefix(prefix);
      name.getPrefix()
          .get(0)
          .addExtension(HL7StructDef.ISO_21090_EN_QUALIFIER.asCodeExtension("AC"));
    }

    return name;
  }

  /**
   * Builds the human name for the "new profiles" with additional extensions
   *
   * @return HumanName according to new profiles
   */
  public HumanName build() {
    val name = buildSimple();

    val tokenized = TokenizedFamilyName.split(family);
    name.getFamilyElement()
        .addExtension(HL7StructDef.HUMAN_OWN_NAME.asStringExtension(tokenized.familyName));

    // set prefix if existent
    tokenized
        .getPrefix()
        .ifPresent(
            familyPrefix ->
                name.getFamilyElement()
                    .addExtension(HL7StructDef.HUMAN_OWN_PREFIX.asStringExtension(familyPrefix)));

    // set name extension if existent
    tokenized
        .getNameExtension()
        .ifPresent(
            nameExtension ->
                name.getFamilyElement()
                    .addExtension(
                        DeBasisProfilStructDef.HUMAN_NAMENSZUSATZ.asStringExtension(
                            nameExtension)));
    return name;
  }

  /**
   * @param nameExtension Namenszusatz
   */
  private record TokenizedFamilyName(
      @Nullable String nameExtension, @Nullable String prefix, String familyName) {
    private static final List<String> KNOWN_PREFIXES = List.of("von", "zu");

    public Optional<String> getPrefix() {
      return Optional.ofNullable(prefix);
    }

    public Optional<String> getNameExtension() {
      return Optional.ofNullable(nameExtension);
    }

    public static TokenizedFamilyName split(String family) {
      if (Strings.isNullOrEmpty(family) || family.isBlank()) {
        throw new BuilderException("Given family name is missing");
      }

      val familyTokens = family.split(" ");

      if (familyTokens.length == 1) {
        // family name does not have any additional prefixes or extensions
        return new TokenizedFamilyName(null, null, family);
      } else if (familyTokens.length == 2) {
        val familyName = familyTokens[1]; // the last element is usually the family name
        if (KNOWN_PREFIXES.contains(familyTokens[0])) {
          // first element seams to be a prefix
          return new TokenizedFamilyName(null, familyTokens[0], familyName);
        } else {
          // the first element is not a known prefix, add this one as name-extension
          return new TokenizedFamilyName(familyTokens[0], null, familyName);
        }
      } else {
        // more than 2 tokens!
        val familyName =
            familyTokens[familyTokens.length - 1]; // the last element is usually the family name
        val nameExtension = familyTokens[0]; // the first element is usually the name extension
        val prefixTokens = Arrays.copyOfRange(familyTokens, 1, familyTokens.length - 1);
        val prefix = String.join(" ", prefixTokens);
        return new TokenizedFamilyName(nameExtension, prefix, familyName);
      }
    }
  }
}
