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

package de.gematik.bbriccs.fhir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.parser.IParser;
import de.gematik.bbriccs.fhir.exceptions.UnsupportedEncodingException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EncodingTypeTest {

  @ParameterizedTest(name = "[{index}] Should decide XML Encoding from input ''{0}''")
  @ValueSource(strings = {"Xml", "XML", "some XML", "person_01.xml"})
  void chooseFromXmlString(String input) {
    assertEquals(EncodingType.XML, EncodingType.fromString(input));
  }

  @ParameterizedTest(name = "[{index}] Should decide JSON Encoding from input ''{0}''")
  @ValueSource(strings = {"Json", "JSON", "some JSON", "test.json"})
  void chooseFromJsonString(String input) {
    assertEquals(EncodingType.JSON, EncodingType.fromString(input));
  }

  @Test
  void toFileExtension() {
    assertEquals("xml", EncodingType.XML.toFileExtension());
    assertEquals("json", EncodingType.JSON.toFileExtension());
  }

  @Test
  void shouldChooseParser() {
    val xmlParser = mock(IParser.class);
    val jsonParser = mock(IParser.class);

    assertEquals(xmlParser, EncodingType.chooseAppropriateParser("xml", xmlParser, jsonParser));
    assertEquals(jsonParser, EncodingType.chooseAppropriateParser("json", xmlParser, jsonParser));
  }

  @ParameterizedTest(name = "[{index}] Given content ''{0}'' is not a valid for encoding types")
  @ValueSource(strings = {"Gson", "GSON", "some GSON", "test.gson", "HTML", "test.html"})
  void chooseThrowOnInvalid(String input) {
    val exception =
        assertThrows(UnsupportedEncodingException.class, () -> EncodingType.fromString(input));
    assertTrue(exception.getMessage().contains("not supported or invalid"));
  }

  @Test
  void shouldFlipEncoding() {
    assertEquals(EncodingType.XML, EncodingType.JSON.flipEncoding());
    assertEquals(EncodingType.JSON, EncodingType.XML.flipEncoding());
  }

  @Test
  void shouldGuessXmlFromContent() {
    val content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    val type = EncodingType.guessFromContent(content);
    assertEquals(EncodingType.XML, type);
  }

  @ParameterizedTest
  @ValueSource(strings = {"{}", "something else"})
  void shouldGuessJsonOnAnyOther(String content) {
    val type = EncodingType.guessFromContent(content);
    assertEquals(EncodingType.JSON, type);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "\n", "\r", "\r\n"})
  void shouldGuessXmlOnEmptyStrings(String content) {
    val type = EncodingType.guessFromContent(content);
    assertEquals(EncodingType.XML, type);
  }
}
