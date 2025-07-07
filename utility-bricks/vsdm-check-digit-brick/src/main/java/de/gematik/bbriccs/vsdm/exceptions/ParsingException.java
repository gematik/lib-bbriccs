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

package de.gematik.bbriccs.vsdm.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.vsdm.VsdmUtils;

public class ParsingException extends RuntimeException {
  public ParsingException(byte[] data, int index) {
    super(
        format(
            "Index {0} is invalid. The data length have to be greater than {1}.",
            index, VsdmUtils.bytesToHex(data)));
  }

  public ParsingException(byte[] data, int start, int end) {
    super(
        format(
            "Index from {0} or to {1} is invalid. The data length have to be greater than {2}.",
            start, end, VsdmUtils.bytesToHex(data)));
  }

  public ParsingException() {
    super(format("Data is null or empty"));
  }
}
