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

package de.gematik.bbriccs.vsdm.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.vsdm.exceptions.ParsingXmlException;
import de.gematik.ws.fa.vsds.PN;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XmlEncoderTest {

  private PN pn;

  @BeforeEach
  void setUp() {
    pn = new PN();
    pn.setCDMVERSION("1.0.0");
    pn.setTS("20260331120000");
    pn.setE(BigInteger.valueOf(1));
  }

  @Test
  void asXmlShouldReturnNonNullString() {
    val xml = assertDoesNotThrow(() -> XmlEncoder.asXml(pn));
    assertNotNull(xml);
    assertFalse(xml.isBlank());
  }

  @Test
  void asXmlShouldContainRootElement() {
    val xml = XmlEncoder.asXml(pn);
    assertTrue(xml.contains("<PN"), "XML sollte <PN>-Wurzelelement enthalten");
  }

  @Test
  void asXmlShouldContainTimestampElement() {
    val xml = XmlEncoder.asXml(pn);
    assertTrue(xml.contains("<TS>20260331120000</TS>"));
  }

  @Test
  void asXmlShouldContainResultElement() {
    val xml = XmlEncoder.asXml(pn);
    assertTrue(xml.contains("<E>1</E>"));
  }

  @Test
  void asXmlShouldContainCdmVersionAttribute() {
    val xml = XmlEncoder.asXml(pn);
    assertTrue(xml.contains("1.0.0"), "XML sollte CDM_VERSION 1.0.0 enthalten");
  }

  @Test
  void asXmlShouldNotContainPzElementWhenNull() {
    val xml = XmlEncoder.asXml(pn);
    assertFalse(xml.contains("<PZ>"), "XML darf kein <PZ>-Element enthalten, wenn pz null ist");
  }

  @Test
  void asXmlShouldContainPzElementWhenSet() {
    pn.setPZ(new byte[] {1, 2, 3});
    val xml = XmlEncoder.asXml(pn);
    assertTrue(xml.contains("<PZ>"), "XML sollte <PZ>-Element enthalten, wenn pz gesetzt ist");
  }

  @Test
  void encodeShouldReturnValidBase64String() {
    val encoded = assertDoesNotThrow(() -> XmlEncoder.encode(pn));
    assertNotNull(encoded);
    assertDoesNotThrow(() -> Base64.getDecoder().decode(encoded));
  }

  @Test
  void encodeShouldProduceGzipCompressedContent() throws IOException {
    val encoded = XmlEncoder.encode(pn);
    val decoded = Base64.getDecoder().decode(encoded);

    // decoded sollte gültiges GZIP-Daten enthalten
    try (val gzipIn = new GZIPInputStream(new ByteArrayInputStream(decoded))) {
      val content = new String(gzipIn.readAllBytes(), StandardCharsets.UTF_8);
      assertFalse(content.isBlank());
      assertTrue(content.contains("<PN"), "Dekomprimierter Inhalt sollte valides XML enthalten");
    }
  }

  @Test
  void parseShouldRoundtripViaStringOverload() {
    val encoded = XmlEncoder.encode(pn);
    val parsed = XmlEncoder.parse(PN.class, encoded);

    assertNotNull(parsed);
    assertEquals(pn.getTS(), parsed.getTS());
    assertEquals(pn.getE(), parsed.getE());
    assertEquals(pn.getCDMVERSION(), parsed.getCDMVERSION());
  }

  @Test
  void parseShouldThrowOnInvalidBase64String() {
    assertThrows(Exception.class, () -> XmlEncoder.parse(PN.class, "kein-valides-base64!!!"));
  }

  @Test
  void parseShouldThrowOnValidBase64ButInvalidGzipContent() {
    // Valides Base64, aber kein GZIP-Inhalt → IOException wird in ParsingXmlException gekapselt
    val notGzip = Base64.getEncoder().encodeToString("not-gzip-content".getBytes());
    val ex = assertThrows(ParsingXmlException.class, () -> XmlEncoder.parse(PN.class, notGzip));
    assertNotNull(ex.getCause(), "Ursache muss die ursprüngliche IOException sein");
    assertInstanceOf(IOException.class, ex.getCause());
  }

  @Test
  void parseShouldWrapJaxbExceptionInXmlEncoderException() throws IOException {
    // Valides Base64+GZIP, aber kein gültiges PN-XML → JAXB schlägt fehl
    val invalidXml = encodeAsBase64Gzip("<invalid>not-a-pn</invalid>");
    val ex = assertThrows(ParsingXmlException.class, () -> XmlEncoder.parse(PN.class, invalidXml));
    assertNotNull(ex.getCause(), "Ursache muss die ursprüngliche JAXBException sein");
    assertTrue(ex.getMessage().contains("PN"), "Fehlermeldung sollte den Zieltyp enthalten");
  }

  @Test
  void parseShouldRoundtripViaByteArrayOverload() {
    val encoded = XmlEncoder.encode(pn);
    val parsed = XmlEncoder.parse(PN.class, encoded.getBytes(StandardCharsets.UTF_8));

    assertNotNull(parsed);
    assertEquals(pn.getTS(), parsed.getTS());
    assertEquals(pn.getE(), parsed.getE());
  }

  @Test
  void parseShouldThrowOnInvalidBase64ByteArray() {
    val invalid = "not-valid!!!".getBytes(StandardCharsets.UTF_8);
    assertThrows(Exception.class, () -> XmlEncoder.parse(PN.class, invalid));
  }

  @Test
  void encodeAndParseShouldPreserveAllFields() {
    pn.setPZ(new byte[] {42, 43, 44});
    pn.setE(BigInteger.valueOf(3));

    val encoded = XmlEncoder.encode(pn);
    val parsed = XmlEncoder.parse(PN.class, encoded);

    assertEquals(pn.getTS(), parsed.getTS());
    assertEquals(pn.getE(), parsed.getE());
    assertEquals(pn.getCDMVERSION(), parsed.getCDMVERSION());
    assertArrayEquals(pn.getPZ(), parsed.getPZ());
  }

  @Test
  void encodeShouldProduceDifferentOutputForDifferentInputs() {
    val pn2 = new PN();
    pn2.setCDMVERSION("1.0.0");
    pn2.setTS("20260101000000");
    pn2.setE(BigInteger.valueOf(2));

    val encoded1 = XmlEncoder.encode(pn);
    val encoded2 = XmlEncoder.encode(pn2);

    assertNotEquals(encoded1, encoded2);
  }

  private String encodeAsBase64Gzip(String content) throws IOException {
    val baos = new ByteArrayOutputStream();
    try (val gzip = new GZIPOutputStream(baos)) {
      gzip.write(content.getBytes(StandardCharsets.UTF_8));
    }
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }
}
