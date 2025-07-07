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

package de.gematik.bbriccs.rest.fd.query;

/**
 * Search prefixes as described in <a href="https://build.fhir.org/search.html#prefix">FHIR.org</a>
 */
public enum SearchPrefix {
  EQ("eq"),
  NE("ne"),
  GT("gt"),
  LT("lt"),
  GE("ge"),
  LE("le"),
  SA("sa"),
  EB("eb"),
  AP("ap");

  private final String prefixCode;

  SearchPrefix(String value) {
    this.prefixCode = value;
  }

  public String value() {
    return this.prefixCode;
  }
}
