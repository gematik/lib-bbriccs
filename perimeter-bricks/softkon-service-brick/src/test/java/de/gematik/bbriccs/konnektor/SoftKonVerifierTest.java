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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.stream.Stream;
import lombok.val;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class SoftKonVerifierTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardArchive.fromResources();
  }

  private static final String SIGNED_DOC_VALID =
      "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";
  private static final String SIGNED_DOC_INVALID =
      "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";

  static Stream<Arguments> shouldNotThrowOnInvalidDataToVerify() {
    return Stream.of(
        Arguments.of((Object) "HelloWorld".getBytes()),
        Arguments.of((Object) Base64.decode(SIGNED_DOC_INVALID)),
        Arguments.of((Object) new byte[0]));
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldNotThrowOnInvalidDataToVerify(byte[] data) {
    boolean isValid = assertDoesNotThrow(() -> SoftKonVerifier.verify(data));
    assertFalse(isValid);
  }

  @Test
  void shouldVerifyValidlySignedDocument() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    boolean isValid = assertDoesNotThrow(() -> SoftKonVerifier.verify(data));
    assertTrue(isValid);
  }

  @Test
  void shouldParseValidDocumentWithoutThrowing() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    assertDoesNotThrow(() -> SoftKonVerifier.parse(data));
  }

  @Test
  void shouldVerifyInstanceMethodOnValidDocument() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    val verifier = SoftKonVerifier.parse(data);
    boolean result = assertDoesNotThrow(() -> (boolean) verifier.verify());
    assertTrue(result);
  }

  @Test
  void getAllDocumentsShouldReturnNonEmptyListForValidDocument() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    val verifier = SoftKonVerifier.parse(data);
    val docs = assertDoesNotThrow(verifier::getAllDocuments);
    assertNotNull(docs);
    assertFalse(docs.isEmpty());
  }

  @Test
  void getFirstDocumentShouldReturnOriginalContentForValidDocument() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    val verifier = SoftKonVerifier.parse(data);
    val first = assertDoesNotThrow(verifier::getFirstDocument);
    assertNotNull(first);
    assertTrue(first.length > 0);
    assertEquals("HelloWorld", new String(first));
  }

  @Test
  void getDocumentShouldReturnOriginalStringForValidDocument() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    val verifier = SoftKonVerifier.parse(data);
    val doc = assertDoesNotThrow(verifier::getDocument);
    assertEquals("HelloWorld", doc);
  }

  @Test
  void getOcspTokensShouldReturnListForValidDocument() {
    val data = Base64.decode(SIGNED_DOC_VALID);
    val verifier = SoftKonVerifier.parse(data);
    val tokens = assertDoesNotThrow(verifier::getOcspTokens);
    assertNotNull(tokens);
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void signAndVerifyRoundtripWithHba(CryptoSystem cryptoSystem) {
    val hba = sca.getHba(0);
    val signer = SoftKonSigner.signQES(hba, cryptoSystem);
    val signed = assertDoesNotThrow(() -> signer.signDocument(false, "HelloWorld"));

    assertTrue(SoftKonVerifier.verify(signed));
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void signedDocumentContainsOriginalContent(CryptoSystem cryptoSystem) {
    val content = "TestContent-" + cryptoSystem.name();
    val hba = sca.getHba(0);
    val signed = SoftKonSigner.signQES(hba, cryptoSystem).signDocument(false, content);

    val verifier = SoftKonVerifier.parse(signed);
    assertEquals(content, verifier.getDocument());
  }

  @ParameterizedTest
  @EnumSource(value = CryptoSystem.class, mode = EnumSource.Mode.EXCLUDE, names = "RSA_PSS_2048")
  void signAndVerifyRoundtripWithSmcb(CryptoSystem cryptoSystem) {
    val smcb = sca.getSmcB(0);
    val content = "SmcBContent";
    val signed =
        assertDoesNotThrow(
            () -> SoftKonSigner.signNonQES(smcb, cryptoSystem).signDocument(false, content));

    // SMC-B Aussteller nicht in BNetzAVLCa → OCSP-Prüfung schlägt fehl, Dokument selbst
    // ist aber korrekt eingebettet
    val verifier = SoftKonVerifier.parse(signed);
    assertEquals(content, verifier.getDocument());
  }

  @Test
  void parseShouldThrowOnCompletelyInvalidInput() {
    // DSS wirft UnsupportedOperationException, wenn das Dokument keinem
    // unterstützten Signaturformat entspricht
    val bytes = "not-a-cms-document".getBytes();
    assertThrows(UnsupportedOperationException.class, () -> SoftKonVerifier.parse(bytes));
  }
}
