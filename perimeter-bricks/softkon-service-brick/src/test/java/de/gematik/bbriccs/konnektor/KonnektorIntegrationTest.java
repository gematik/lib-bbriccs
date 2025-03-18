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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.cardterminal.CardInfo;
import de.gematik.bbriccs.cardterminal.CardTerminal;
import de.gematik.bbriccs.cardterminal.PinType;
import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.cfg.KonnektorContextConfiguration;
import de.gematik.bbriccs.konnektor.cfg.KonnektorServiceConfiguration;
import de.gematik.bbriccs.konnektor.cfg.SoftKonServiceConfiguration;
import de.gematik.bbriccs.konnektor.exceptions.MissingKonnektorServiceException;
import de.gematik.bbriccs.konnektor.exceptions.SOAPRequestException;
import de.gematik.bbriccs.konnektor.exceptions.SmartcardMissmatchException;
import de.gematik.bbriccs.konnektor.requests.*;
import de.gematik.bbriccs.konnektor.vsdm.VsdmService;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardP12;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import java.security.*;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class KonnektorIntegrationTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardArchive.fromResources();
  }

  private Konnektor createTestKonnektor() {
    return createTestKonnektor(sca);
  }

  private Konnektor createTestKonnektor(SmartcardArchive smartcardArchive) {
    val serviceProvider =
        new SofKonServicePort(smartcardArchive, VsdmService.instantiateWithTestKey());
    return new KonnektorImpl(
        KonnektorContextConfiguration.getDefaultContextType(), serviceProvider, List.of());
  }

  @Test
  void shouldInstantiateSoftKon() {
    val serviceProvider = new SofKonServicePort(sca, VsdmService.instantiateWithTestKey());
    val ctx = KonnektorContextConfiguration.getDefaultContextType();
    assertDoesNotThrow(
        () -> new KonnektorImpl(ctx, serviceProvider, List.of(mock(CardTerminal.class))));
  }

  @Test
  void shouldInstantiateSoftKonFromConfig() {
    val scfg = new SoftKonServiceConfiguration();
    scfg.setType("Soft-Kon");
    scfg.setSmartcards(ResourceLoader.getFileFromResource("smartcards").getAbsolutePath());
    val cfg = new KonnektorConfiguration();
    cfg.setContext(new KonnektorContextConfiguration());
    cfg.setService(scfg);
    assertDoesNotThrow(() -> Konnektor.create(cfg));
  }

  @ParameterizedTest
  @EnumSource(CryptoSystem.class)
  void shouldPerformSimpleRoundtripOnSoftKonnektor(CryptoSystem cryptoSystem) {
    val konnektor = createTestKonnektor();

    val cardHandle =
        konnektor.execute(GetCardHandleRequest.forSmartcard(sca.getHba(0))).getPayload();

    // read the Auth certificate from card
    val cardAuthCertCmd =
        new ReadCardCertificateRequest(cardHandle, CertRefEnum.C_AUT, cryptoSystem);
    val cardAuthCertificate = konnektor.execute(cardAuthCertCmd).getPayload();
    assertNotNull(cardAuthCertificate);

    // read the Sig certificate from card
    val cardSigCertCmd = new ReadCardCertificateRequest(cardHandle, cryptoSystem);
    val cardSigCertificate = konnektor.execute(cardSigCertCmd);
    assertNotNull(cardSigCertificate);

    // read the Sig certificate from card
    val cardEncCertCmd =
        new ReadCardCertificateRequest(cardHandle, CertRefEnum.C_AUT, cryptoSystem);
    val cardEncCertificate = konnektor.execute(cardEncCertCmd);
    assertNotNull(cardEncCertificate);

    // read the Sig certificate from card
    val cardQesCertCmd =
        new ReadCardCertificateRequest(cardHandle, CertRefEnum.C_AUT, cryptoSystem);
    val cardQesCertificate = konnektor.execute(cardQesCertCmd);
    assertNotNull(cardQesCertificate);

    // sign an exemplary XML document
    val signCmd = new SignXMLDocumentRequest(cardHandle, "<xml>TEST</xml>", cryptoSystem);
    val signRsp = konnektor.execute(signCmd).getPayload();
    assertNotNull(signRsp);
    assertTrue(signRsp.length > 0);

    // verify the document from previous step
    val verifyCmd = new VerifyDocumentRequest(signRsp);
    val verifyRsp = konnektor.execute(verifyCmd).getPayload();
    assertTrue(verifyRsp);
  }

  @ParameterizedTest
  @EnumSource(CryptoSystem.class)
  void shouldExternallyAuthenticate(CryptoSystem cryptoSystem) {
    val konnektor = createTestKonnektor();

    val cardHandle =
        konnektor.execute(GetCardHandleRequest.forSmartcard(sca.getSmcB(0))).getPayload();

    val extAuthCmd =
        new ExternalAuthenticateRequest(cardHandle, cryptoSystem, "challenge".getBytes());
    val token = konnektor.execute(extAuthCmd);
    assertNotNull(token);
    assertTrue(token.getPayload().length > 0);
  }

  @ParameterizedTest
  @EnumSource(CryptoSystem.class)
  void shouldThrowOnExternalAuthenticateWithUnknownCardHandle(CryptoSystem cryptoSystem) {
    val konnektor = createTestKonnektor();

    val cardHandle = CardInfo.builder().handle("abc").build();

    val extAuthCmd =
        new ExternalAuthenticateRequest(cardHandle, cryptoSystem, "challenge".getBytes());
    val token = konnektor.executeSafely(extAuthCmd);
    assertNotNull(token);
    assertTrue(token.isEmpty());
  }

  @ParameterizedTest
  @EnumSource(CryptoSystem.class)
  void shouldThrowOnExternalAuthenticateWithMissingAuthCertificate(CryptoSystem cryptoSystem) {
    val mockArchive = mock(SmartcardArchive.class);
    when(mockArchive.getByICCSN(any(), anyString())).thenReturn(mock(SmartcardP12.class));

    val konnektor = createTestKonnektor(mockArchive);

    val cardHandle = CardInfo.builder().handle("abc").build();
    val extAuthCmd =
        new ExternalAuthenticateRequest(cardHandle, cryptoSystem, "challenge".getBytes());
    val token = konnektor.executeSafely(extAuthCmd);
    assertNotNull(token);
    assertTrue(token.isEmpty());
  }

  @Test
  void shouldReadVsd() {
    val konnektor = createTestKonnektor();

    val egkHandle =
        konnektor.execute(GetCardHandleRequest.forIccsn("80276883110000113311")).getPayload();
    val hbaHandle =
        konnektor.execute(GetCardHandleRequest.forIccsn("80276001011699901501")).getPayload();
    val request = new ReadVsdRequest(egkHandle, hbaHandle, true, true);
    val response = assertDoesNotThrow(() -> konnektor.execute(request));
    assertNotNull(response.getPayload());
  }

  @Test
  void shouldThrowOnReadVsdForUnknownSmartcard() {
    val konnektor = createTestKonnektor();

    val cit = new CardInfoType();
    cit.setCardType(CardTypeType.EGK);
    cit.setCardHandle("123");
    val egkHandle = CardInfo.fromCardInfoType(cit);
    val hbaHandle =
        konnektor.execute(GetCardHandleRequest.forIccsn("80276001011699901501")).getPayload();
    val request = new ReadVsdRequest(egkHandle, hbaHandle, true, true);
    assertThrows(SOAPRequestException.class, () -> konnektor.execute(request));
  }

  @ParameterizedTest
  @EnumSource(CryptoSystem.class)
  void shouldEncryptAndDecryptMessage(CryptoSystem cryptoSystem) {
    val konnektor = createTestKonnektor();

    val cardInfo =
        konnektor.execute(GetCardHandleRequest.forIccsn("80276001011699901501")).getPayload();

    val encryptRequest =
        new EncryptDocumentRequest(cardInfo, "HelloWorld".getBytes(), cryptoSystem);
    val encryptResponse = assertDoesNotThrow(() -> konnektor.execute(encryptRequest));
    val encrypted = encryptResponse.getPayload();

    val decryptRequest = new DecryptDocumentRequest(cardInfo, encrypted, cryptoSystem);
    val decryptResponse = assertDoesNotThrow(() -> konnektor.execute(decryptRequest));
    val decrypted = decryptResponse.getPayload();
    assertEquals("HelloWorld", new String(decrypted));
  }

  @ParameterizedTest
  @EnumSource(CryptoSystem.class)
  void shouldSignAndVerifyDocument(CryptoSystem cryptoSystem) {
    val konnektor = createTestKonnektor();

    val signer = konnektor.execute(GetCardHandleRequest.forSmartcard(sca.getHba(0))).getPayload();

    val signRequest = new SignXMLDocumentRequest(signer, "<xml>TEST</xml>", cryptoSystem);
    val signResponse = assertDoesNotThrow(() -> konnektor.execute(signRequest));
    val signed = signResponse.getPayload();

    val verifyRequest = new VerifyDocumentRequest(signed);
    val verifyResponse = assertDoesNotThrow(() -> konnektor.execute(verifyRequest));
    assertTrue(verifyResponse.getPayload());
  }

  @Test
  void shouldFailOnVerifyUnsigned() {
    val konnektor = createTestKonnektor();

    val verifyRequest = new VerifyDocumentRequest("unsigned".getBytes());
    val verifyResponse = assertDoesNotThrow(() -> konnektor.execute(verifyRequest));
    assertFalse(verifyResponse.getPayload());
  }

  @Test
  void shouldFailOnVerifyWronglySigned()
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    val konnektor = createTestKonnektor();

    val kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    val keyPair = kpg.genKeyPair();

    val sig = Signature.getInstance("SHA1WithRSA");
    sig.initSign(keyPair.getPrivate());
    sig.update("HelloWorld".getBytes());
    byte[] signed = sig.sign();

    val verifyRequest = new VerifyDocumentRequest(signed);
    val verifyResponse = assertDoesNotThrow(() -> konnektor.execute(verifyRequest));
    assertFalse(verifyResponse.getPayload());
  }

  @Test
  void shouldVerifyPin() {
    val konnektor = createTestKonnektor();

    val cardInfo =
        konnektor.execute(GetCardHandleRequest.forIccsn("80276001011699901501")).getPayload();
    val request = new VerifyPinRequest(cardInfo, PinType.PIN_CH);
    val response = assertDoesNotThrow(() -> konnektor.execute(request));
    assertNotNull(response.getPayload());
  }

  @Test
  void shouldSignEmptyDocumentsListWithoutThrowing() {
    val konnektor = createTestKonnektor();

    val cardInfo =
        konnektor.execute(GetCardHandleRequest.forIccsn("80276001011699901501")).getPayload();

    val request = new SignDocumentsRequest(cardInfo.getHandle(), List.of());
    val response = assertDoesNotThrow(() -> konnektor.execute(request));
    assertNotNull(response.getPayload());
    assertTrue(response.getPayload().isEmpty());
  }

  @Test
  void shouldThrowOnUnknownSmartcard() {
    val konnektor = createTestKonnektor();

    val request = GetCardHandleRequest.forIccsn("111111111111");
    assertThrows(SmartcardMissmatchException.class, () -> konnektor.execute(request));
  }

  @Test
  void shouldReceiveResponse() {
    val mockCmd = mock(GetCardHandleRequest.class);
    val cit = new CardInfoType();
    cit.setIccsn("80276001011699910102");
    cit.setCardHandle("my_test_handle");
    cit.setCtId("Ct01");
    cit.setCardType(CardTypeType.HBA);
    val cardHandle = CardInfo.fromCardInfoType(cit);
    when(mockCmd.execute(any(), any())).thenReturn(cardHandle);

    val serviceProvider = new SofKonServicePort(sca, VsdmService.instantiateWithTestKey());
    val konnektor =
        new KonnektorImpl(
            KonnektorContextConfiguration.getDefaultContextType(), serviceProvider, List.of());
    val response = konnektor.execute(mockCmd).getPayload();
    assertEquals(cardHandle, response);
  }

  @Test
  void shouldReceiveResponseSafely() {
    val mockCmd = mock(GetCardHandleRequest.class);
    val cit = new CardInfoType();
    cit.setIccsn("80276001011699910102");
    cit.setCardHandle("my_test_handle");
    cit.setCtId("Ct01");
    cit.setCardType(CardTypeType.HBA);
    val cardHandle = CardInfo.fromCardInfoType(cit);
    when(mockCmd.execute(any(), any())).thenReturn(cardHandle);

    val serviceProvider = new SofKonServicePort(sca, VsdmService.instantiateWithTestKey());
    val konnektor =
        new KonnektorImpl(
            KonnektorContextConfiguration.getDefaultContextType(), serviceProvider, List.of());
    val response = konnektor.executeSafely(mockCmd);
    assertTrue(response.isPresent());
    assertEquals(cardHandle, response.orElseThrow().getPayload());
  }

  @Test
  void shouldSafelyCatchSoapErrors() {
    val mockCmd = mock(GetCardHandleRequest.class);
    when(mockCmd.execute(any(), any())).thenThrow(SOAPRequestException.class);

    val serviceProvider = new SofKonServicePort(sca, VsdmService.instantiateWithTestKey());
    val konnektor =
        new KonnektorImpl(
            KonnektorContextConfiguration.getDefaultContextType(), serviceProvider, List.of());
    val response = Assertions.assertDoesNotThrow(() -> konnektor.executeSafely(mockCmd));
    assertTrue(response.isEmpty());
  }

  @Test
  void shouldThrowOnMissingKonnektorService() {
    val cfg = new KonnektorConfiguration();
    val serviceCfg = new ConcreteKonnektorServiceConfiguration();
    serviceCfg.setType("UnknownService");
    cfg.setName("Test Konnektor");
    cfg.setService(serviceCfg);

    val exception =
        assertThrows(MissingKonnektorServiceException.class, () -> Konnektor.create(cfg));
    assertTrue(exception.getMessage().contains("UnknownService"));
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  private static class ConcreteKonnektorServiceConfiguration extends KonnektorServiceConfiguration {
    private String testField = "test field";
  }
}
