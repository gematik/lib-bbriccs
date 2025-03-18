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

package de.gematik.bbriccs.konnektor.vsdm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.konnektor.exceptions.ParsingExamEvidenceException;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VsdmExamEvidenceTest {

  static Stream<Arguments> builderFunctionProvider() {
    return Stream.of(
        Arguments.of(
            (Function<VsdmExamEvidence.VsdmExamEvidenceBuilder, VsdmExamEvidence>)
                builder ->
                    builder
                        .withExpiredTimestamp()
                        .checksumWithUpdateReason(VsdmUpdateReason.CARD_MANAGEMENT_UPDATE)
                        .generate(VsdmExamEvidenceResult.NO_UPDATES)),
        Arguments.of(
            (Function<VsdmExamEvidence.VsdmExamEvidenceBuilder, VsdmExamEvidence>)
                builder ->
                    builder
                        .withInvalidTimestamp()
                        .checksumWithUpdateReason(VsdmUpdateReason.UFS_UPDATE)
                        .checksumWithInvalidManufacturer()
                        .checksumWithInvalidKvnr()
                        .checksumWithInvalidVersion()
                        .generate(VsdmExamEvidenceResult.NO_UPDATES)));
  }

  @ParameterizedTest
  @MethodSource("builderFunctionProvider")
  void shouldBuildAsOfflineMode(
      Function<VsdmExamEvidence.VsdmExamEvidenceBuilder, VsdmExamEvidence> builderFunction) {
    val evidenceBuilder = VsdmExamEvidence.asOfflineMode();
    val evidence = builderFunction.apply(evidenceBuilder);

    assertFalse(evidence.getChecksum().isPresent());
    assertNotNull(evidence.encodeAsBase64());
    assertFalse(evidence.encodeAsBase64().isEmpty());
    assertNotNull(evidence.getCdmVersion());
    assertFalse(evidence.getCdmVersion().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("builderFunctionProvider")
  void shouldBuildAsOnlineTestMode(
      Function<VsdmExamEvidence.VsdmExamEvidenceBuilder, VsdmExamEvidence> builderFunction) {
    val sca = SmartcardArchive.fromResources();
    val evidenceBuilder = VsdmExamEvidence.asOnlineTestMode(sca.getEgk(0));
    val evidence = builderFunction.apply(evidenceBuilder);

    assertTrue(evidence.getChecksum().isPresent());
    assertNotNull(evidence.encodeAsBase64());
    assertFalse(evidence.encodeAsBase64().isEmpty());
    assertNotNull(evidence.getCdmVersion());
    assertFalse(evidence.getCdmVersion().isEmpty());
    assertDoesNotThrow(evidence::toString);
  }

  @Test
  void shouldParseFromBase64() {
    val b64 =
        "H4sIAAAAAAAA/x2NQU/DIABG/0rD3UIpM5sBlimoMyl1Fjn0YjqLW+dKp9S226+X7PIdXvK9R5dTe4wG++ubzjGQxAhE1n12deN2DLzrx5s5iHxfubo6ds4ycLYeLDl9VVE4Os/Avu9PdxCOPt7Ztuqb77i28KuCg69beHIjHK7SB5F9GPlWrHN1zQTGqS44RpigNFlgRBbJjMKAqOSYQhkiJTdihdRBouywmeVapplekVzIi9LGGPFM1rBMVSOwKObj9q91T5dyP93m0wv52d5vPKMwSMIo/g9W9xM96gAAAA==";
    val checksum = "VDA0NjE0MjQ5OTE3MTA4ODEzNTVVVDH4I/Z3NiD2DS8wbumnGzZhx6OxJ4qbBQs=";
    val evidence = assertDoesNotThrow(() -> VsdmExamEvidence.parse(b64));
    assertTrue(evidence.getChecksum().isPresent());
    assertEquals(checksum, evidence.getChecksum().orElseThrow());
  }

  @Test
  void shouldThrowOnInvalidExamEvidence() {
    assertThrows(ParsingExamEvidenceException.class, () -> VsdmExamEvidence.parse("invalid"));
  }

  @Test
  void shouldReThrowOnCompress() {
    val evidence = VsdmExamEvidence.asOfflineMode().generate(VsdmExamEvidenceResult.NO_UPDATES);

    try (val mc =
        mockConstruction(
            GZIPOutputStream.class,
            (mock, context) -> doThrow(new IOException("TEST")).when(mock).write(any()))) {
      val ioe = assertThrows(IOException.class, evidence::encode);
      assertTrue(ioe.getMessage().contains("TEST"));
    }
  }
}
