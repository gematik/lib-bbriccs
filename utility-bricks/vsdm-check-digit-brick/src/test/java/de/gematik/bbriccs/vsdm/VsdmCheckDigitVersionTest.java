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

package de.gematik.bbriccs.vsdm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VsdmCheckDigitVersionTest {

  @Test
  void shouldReturnCorrectVersionForByte() {
    assertEquals(
        VsdmCheckDigitVersion.V1, VsdmCheckDigitVersion.fromData(new byte[] {(byte) 0x01}));
    assertEquals(
        VsdmCheckDigitVersion.V1, VsdmCheckDigitVersion.fromData(new byte[] {(byte) 0x80}));
    assertEquals(
        VsdmCheckDigitVersion.V2, VsdmCheckDigitVersion.fromData(new byte[] {(byte) 0x81}));
    assertEquals(
        VsdmCheckDigitVersion.V2, VsdmCheckDigitVersion.fromData(new byte[] {(byte) 0xDA}));
  }

  @Test
  void shouldReturnCorrectVersionForBase64() {
    assertEquals(
        VsdmCheckDigitVersion.V1,
        VsdmCheckDigitVersion.fromData(
            "WTc4NTcyODA3MTE2ODU0NDA4MzdVQzEpQdKViiyA4SGBIjkJuPVMWhLD6OBwggI="));
    assertEquals(
        VsdmCheckDigitVersion.V2,
        VsdmCheckDigitVersion.fromData(
            "wbGE920hMl4Hjm4oNc624ocDLdqzh8S6UA5LB7Q/iTYYT7JxUumHh6n+T2vdyd0="));
  }

  @Test
  void shouldThrowException() {
    final byte[] dataNull = null;
    assertThrows(IllegalArgumentException.class, () -> VsdmCheckDigitVersion.fromData(dataNull));
    final byte[] dataEmpty = new byte[0];
    assertThrows(IllegalArgumentException.class, () -> VsdmCheckDigitVersion.fromData(dataEmpty));
  }
}
