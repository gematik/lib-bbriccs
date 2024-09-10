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

package de.gematik.bbriccs.smartcards;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.exceptions.SmartcardFactoryException;
import java.util.LinkedList;
import java.util.List;
import lombok.*;

@Getter
public class SmartcardOwnerData {

  private final String commonName;
  private final String title;
  private final String givenName;
  private final String surname;
  private final String organization;
  private final List<String> organizationUnit;
  private final String street;
  private final String locality;
  private final String postalCode;
  private final String country;

  private SmartcardOwnerData(Builder builder) {
    this.commonName = builder.commonName;
    this.title = builder.title;
    this.givenName = builder.givenName;
    this.surname = builder.surname;
    this.organization = builder.organization;
    this.organizationUnit = builder.organizationUnit;
    this.street = builder.street;
    this.locality = builder.locality;
    this.postalCode = builder.postalCode;
    this.country = builder.country;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getOwnerName() {
    val ret = (title != null) ? title + " " : "";
    return ret + format("{0}, {1}", givenName, surname);
  }

  @Override
  public String toString() {
    return getOwnerName();
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private String commonName;
    private String title;
    private String givenName;
    private String surname;
    private String organization;
    private final List<String> organizationUnit = new LinkedList<>();
    private String street;
    private String locality;
    private String postalCode;
    private String country;

    public Builder commonName(String commonName) {
      this.commonName = commonName;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder givenName(String givenName) {
      this.givenName = givenName;
      return this;
    }

    public Builder surname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder organization(String organization) {
      this.organization = organization;
      return this;
    }

    public Builder organizationUnit(String organizationUnit) {
      this.organizationUnit.add(organizationUnit);
      return this;
    }

    public Builder street(String street) {
      this.street = street;
      return this;
    }

    public Builder locality(String locality) {
      this.locality = locality;
      return this;
    }

    public Builder postalCode(String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    public Builder country(String country) {
      this.country = country;
      return this;
    }

    public SmartcardOwnerData build() {
      if (commonName != null && (givenName == null || surname == null)) {
        val commonNameTokens = commonName.replace("TEST-ONLY", "").trim().split(" ");
        if (givenName == null) {
          givenName = commonNameTokens[0];
        }
        if (surname == null) {
          surname = commonNameTokens[commonNameTokens.length - 1];
        }
      } else if (commonName == null && givenName != null && surname != null) {
        commonName = format("{0} {1}", givenName, surname);
      } else if (commonName == null) {
        throw new SmartcardFactoryException(
            format(
                "Ownerdata does not have enough information to build the full name of the owner"));
      }

      return new SmartcardOwnerData(this);
    }
  }
}
