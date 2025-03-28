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

package de.gematik.bbriccs.konnektor.exceptions;

import static java.text.MessageFormat.format;

import java.util.Base64;

public class ParsingUpdateReasonException extends RuntimeException {
  public ParsingUpdateReasonException(char value) {
    super(format("Checksum value ''{0}'' is not a valid literal of VSDM Update Reasons", value));
  }

  public ParsingUpdateReasonException(byte[] data, int pos) {
    super(
        format(
            "Checksum information {0} could not be parsed at position {1}",
            Base64.getEncoder().encodeToString(data), pos));
  }

  public ParsingUpdateReasonException(byte[] data, int from, int to) {
    super(
        format(
            "Checksum information {0} could not be parsed from position {1} to {2}",
            Base64.getEncoder().encodeToString(data), from, to));
  }
}
