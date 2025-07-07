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

package de.gematik.bbriccs.konnektor.vsdm;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.konnektor.cfg.VsdmServiceConfiguration;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VsdmServiceTest {

  static Stream<Arguments> shouldSignWithChecksums() {
    return Stream.of(
        Arguments.of((BiFunction<VsdmService, String, VsdmChecksum>) VsdmService::checksumFor),
        Arguments.of(
            (BiFunction<VsdmService, String, VsdmChecksum>)
                VsdmService::checksumWithInvalidManufacturer),
        Arguments.of(
            (BiFunction<VsdmService, String, VsdmChecksum>)
                VsdmService::checksumWithInvalidVersion));
  }

  @Test
  void shouldCreateFromConfig() {
    val cfg = VsdmServiceConfiguration.createDefault();
    assertDoesNotThrow(() -> VsdmService.createFrom(cfg));
  }

  @ParameterizedTest
  @MethodSource
  void shouldSignWithChecksums(BiFunction<VsdmService, String, VsdmChecksum> biFunction) {
    val cfg = VsdmServiceConfiguration.createDefault();
    val vsdm = assertDoesNotThrow(() -> VsdmService.createFrom(cfg));
    val checksum = assertDoesNotThrow(() -> biFunction.apply(vsdm, "X117170400"));
    assertDoesNotThrow(() -> vsdm.sign(checksum));
  }
}
