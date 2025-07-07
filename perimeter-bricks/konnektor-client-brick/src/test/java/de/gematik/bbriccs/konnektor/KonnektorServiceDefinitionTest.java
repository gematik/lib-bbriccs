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

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.ws.conn.servicedirectory.ConnectorServices;
import jakarta.xml.bind.JAXBContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KonnektorServiceDefinitionTest {

  static Stream<Arguments> shouldReadSds() {
    return ResourceLoader.getResourceFilesInDirectory("sds").stream()
        .map(f -> Arguments.of(readSds("sds/" + f.getName())));
  }

  @SneakyThrows
  private static ConnectorServices readSds(String fileName) {
    val sds = ResourceLoader.getFileFromResourceAsStream(fileName);

    val jaxb = JAXBContext.newInstance(ConnectorServices.class);
    val unmarshaller = jaxb.createUnmarshaller();
    return (ConnectorServices)
        unmarshaller.unmarshal(new BufferedReader(new InputStreamReader(sds)));
  }

  @Test
  void shouldCreateFromConnectorSds() {
    val connectorServices = readSds("sds/rise_connector_sds.xml");

    val ksd = KonnektorServiceDefinition.from(connectorServices);
    assertNotNull(ksd);
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/cardservice", ksd.getCardService());
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/certificateservice",
        ksd.getCertificateService());
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/eventservice", ksd.getEventService());
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/encryptionservice",
        ksd.getEncryptionService());
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/cardterminalservice",
        ksd.getCardTerminalService());
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/authsignatureservice",
        ksd.getAuthSignatureService());
    assertEquals(
        "https://ksp.ltuzd.telematik-test/kon8/webservices/signatureservice/v7.5",
        ksd.getSignatureService());
    assertEquals("https://ksp.ltuzd.telematik-test/kon8/fm/vsdservice", ksd.getVsdService());
  }

  @ParameterizedTest
  @MethodSource
  void shouldReadSds(ConnectorServices connectorServices) {
    val ksd = KonnektorServiceDefinition.from(connectorServices);
    assertNotNull(ksd);
    assertNotNull(ksd.getCardService());
    assertNotNull(ksd.getCertificateService());
    assertNotNull(ksd.getEventService());
    assertNotNull(ksd.getEncryptionService());
    assertNotNull(ksd.getCardTerminalService());
    assertNotNull(ksd.getAuthSignatureService());
    assertNotNull(ksd.getSignatureService());
    assertNotNull(ksd.getVsdService());
  }

  @Test
  void shouldCreateForSoftKon() {
    val ksd = KonnektorServiceDefinition.forSoftKon();
    assertNotNull(ksd);
    assertNotNull(ksd.getCardService());
    assertNotNull(ksd.getCertificateService());
    assertNotNull(ksd.getEventService());
    assertNotNull(ksd.getEncryptionService());
    assertNotNull(ksd.getCardTerminalService());
    assertNotNull(ksd.getAuthSignatureService());
    assertNotNull(ksd.getSignatureService());
    assertNotNull(ksd.getVsdService());
  }
}
