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

import de.gematik.bbriccs.smartcards.exceptions.InvalidFileExtensionException;
import lombok.Getter;
import lombok.val;

@Getter
public enum KeystoreType {
  P12("PKCS12", ".p12"),
  JKS("JKS", ".jks");

  private final String name;

  private final String fileExtension;

  KeystoreType(String name, String extension) {
    this.name = name;
    this.fileExtension = extension;
  }

  public static KeystoreType fromFileExtension(String fileName) {
    val tokens = fileName.split("\\.");
    val ext = tokens.length >= 2 ? tokens[1] : tokens[0];
    return switch (ext.toLowerCase()) {
      case "p12" -> P12;
      case "jks" -> JKS;
      default -> throw new InvalidFileExtensionException(fileName);
    };
  }
}
