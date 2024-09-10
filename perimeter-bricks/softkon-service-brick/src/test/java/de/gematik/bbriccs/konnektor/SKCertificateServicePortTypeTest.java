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

package de.gematik.bbriccs.konnektor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.konnektor.exceptions.SmartcardException;
import de.gematik.bbriccs.smartcards.*;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate;
import de.gematik.ws.conn.certificateservice.wsdl.v6.FaultMessage;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import jakarta.xml.ws.Holder;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SKCertificateServicePortTypeTest {

  private static ContextType ctx;
  private static SofKonServicePort softKonServiceProvider;

  @BeforeAll
  static void setup() {
    val smartCardArchive = SmartcardArchive.fromResources();
    softKonServiceProvider = new SofKonServicePort(smartCardArchive);

    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");
  }

  @Test
  void shouldThrowExceptions() {
    val eventService = softKonServiceProvider.getCertificateService();
    assertThrows(
        NotImplementedException.class,
        () -> eventService.checkCertificateExpiration("", ctx, null, null, null));

    assertThrows(
        NotImplementedException.class,
        () -> eventService.verifyCertificate(ctx, null, null, null, null, null));
  }

  @Test
  void shouldThrowOnUnknownCardHandle() {
    val eventService = softKonServiceProvider.getCertificateService();
    assertThrows(
        FaultMessage.class,
        () -> eventService.readCardCertificate("unknown", ctx, null, null, null, null));
  }

  @Test
  void shouldErrorOnInvalidSmartcardCertificates() {
    val sca = mock(SmartcardArchive.class);

    val smartcardConfig = new SmartcardConfigDto();
    smartcardConfig.setIccsn("123");
    smartcardConfig.setType(SmartcardType.EGK);
    when(sca.getConfigs()).thenReturn(List.of(smartcardConfig));

    val mockEgk = mock(EgkP12.class);
    when(sca.getByICCSN(eq(SmartcardP12.class), anyString())).thenReturn(mockEgk);

    val servicePort = new SofKonServicePort(sca);
    val eventService = servicePort.getCertificateService();

    val certRefList = new ReadCardCertificate.CertRefList();
    certRefList.getCertRef().add(CertRefEnum.C_ENC);
    val status = new Holder<Status>();
    val dataInfoList = new Holder<X509DataInfoListType>();
    assertDoesNotThrow(
        () ->
            eventService.readCardCertificate(
                "eGK_123", ctx, certRefList, null, status, dataInfoList));
    assertNull(status.value.getResult());
    assertNotNull(status.value.getError());
    assertTrue(status.value.getError().getMessageID().contains("C_ENC is not yet implemented"));
  }

  @Test
  void shouldThrowOnInvalidAuthCertificate() throws CertificateEncodingException {
    val sca = mock(SmartcardArchive.class);

    val smartcardConfig = new SmartcardConfigDto();
    smartcardConfig.setIccsn("123");
    smartcardConfig.setType(SmartcardType.EGK);
    when(sca.getConfigs()).thenReturn(List.of(smartcardConfig));

    val mockSmartcardCertificate = mock(SmartcardCertificateP12.class);
    val mockX509 = mock(X509Certificate.class);
    when(mockSmartcardCertificate.getX509Certificate()).thenReturn(mockX509);
    when(mockX509.getEncoded()).thenThrow(new CertificateEncodingException());

    val mockEgk = mock(EgkP12.class);
    when(mockEgk.getAutCertificate()).thenReturn(mockSmartcardCertificate);
    when(sca.getByICCSN(eq(SmartcardP12.class), anyString())).thenReturn(mockEgk);

    val servicePort = new SofKonServicePort(sca);
    val eventService = servicePort.getCertificateService();

    val certRefList = new ReadCardCertificate.CertRefList();
    certRefList.getCertRef().add(CertRefEnum.C_AUT);
    val status = new Holder<Status>();
    val dataInfoList = new Holder<X509DataInfoListType>();
    assertThrows(
        SmartcardException.class,
        () ->
            eventService.readCardCertificate(
                "eGK_123", ctx, certRefList, null, status, dataInfoList));
  }
}
