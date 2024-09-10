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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.certificate.Oid;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SmartcardArchiveTest {

  private static SmartcardArchive providerArchive;

  @BeforeAll
  static void setup() {
    providerArchive = SmartcardArchive.fromResources();
  }

  static Stream<Arguments> shouldGetSmartcardByType() {
    return Stream.of(
        Arguments.of(SmartcardType.EGK, providerArchive.getICCSNsFor(SmartcardType.EGK).get(0)),
        Arguments.of(SmartcardType.HBA, providerArchive.getICCSNsFor(SmartcardType.HBA).get(0)),
        Arguments.of(
            SmartcardType.SMC_B, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)));
  }

  static Stream<Arguments> shouldGetSmartcardByClassType() {
    return Stream.of(
        Arguments.of(Egk.class, providerArchive.getICCSNsFor(SmartcardType.EGK).get(0)),
        Arguments.of(EgkP12.class, providerArchive.getICCSNsFor(SmartcardType.EGK).get(0)),
        Arguments.of(Hba.class, providerArchive.getICCSNsFor(SmartcardType.HBA).get(0)),
        Arguments.of(HbaP12.class, providerArchive.getICCSNsFor(SmartcardType.HBA).get(0)),
        Arguments.of(SmcB.class, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)),
        Arguments.of(SmcBP12.class, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)),
        Arguments.of(
            InstituteSmartcard.class, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)),
        Arguments.of(
            InstituteSmartcardP12.class, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)),
        Arguments.of(Smartcard.class, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)),
        Arguments.of(SmartcardP12.class, providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0)));
  }

  @SneakyThrows
  @SuppressWarnings({"java:S3011", "unchecked"})
  public static void resetArchiveCache() {
    val instance = SmartcardArchive.class.getDeclaredField("ARCHIVE_CACHE");
    instance.setAccessible(true);
    val cache = (HashMap<File, SmartcardArchive>) instance.get(null);
    cache.clear();
  }

  @ParameterizedTest
  @MethodSource
  void shouldGetSmartcardByClassType(Class<? extends Smartcard> type, String iccsn) {
    val archive = SmartcardArchive.fromResources();
    val smartcard = assertDoesNotThrow(() -> archive.getByICCSN(type, iccsn));
    assertEquals(iccsn, smartcard.getIccsn());
  }

  @ParameterizedTest
  @MethodSource("shouldGetSmartcardByClassType")
  void shouldGetSmartcardWithoutClassType(Class<? extends Smartcard> type, String iccsn) {
    val archive = SmartcardArchive.fromResources();
    val smartcard = assertDoesNotThrow(() -> archive.getByICCSN(iccsn));
    assertTrue(smartcard.isPresent());
    assertEquals(iccsn, smartcard.get().getIccsn());
  }

  @ParameterizedTest
  @MethodSource
  void shouldGetSmartcardByType(SmartcardType type, String iccsn) {
    val archive = SmartcardArchive.fromResources();
    val smartcard = assertDoesNotThrow(() -> archive.getSmartcardByICCSN(type, iccsn));
    assertEquals(iccsn, smartcard.getIccsn());
  }

  @Test
  void shouldThrowOnUnsupportedSmartcardType() {
    val archive = SmartcardArchive.fromResources();
    val iccsn = providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0);
    assertThrows(
        InvalidSmartcardTypeException.class,
        () -> archive.getSmartcardByICCSN(SmartcardType.SMC_KT, iccsn));
  }

  @Test
  void shouldThrowOnUnsupportedSmartcardTypeClass() {
    val archive = SmartcardArchive.fromResources();
    val iccsn = providerArchive.getICCSNsFor(SmartcardType.SMC_B).get(0);
    assertThrows(
        InvalidSmartcardTypeException.class, () -> archive.getByICCSN(TestSmartcard.class, iccsn));
  }

  @Test
  void shouldThrowOnUnknownSmartcard() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    val archive = SmartcardArchive.from(archiveFile);

    assertDoesNotThrow(() -> archive.getICCSNsFor(SmartcardType.EGK));
    assertDoesNotThrow(() -> archive.getICCSNsFor(SmartcardType.HBA));
    assertDoesNotThrow(() -> archive.getICCSNsFor(SmartcardType.SMC_B));
    assertThrows(CardNotFoundException.class, () -> archive.getEgkByICCSN("123"));
  }

  @Test
  void shouldThrowOnUnknownKvnr() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    val archive = SmartcardArchive.from(archiveFile);
    assertThrows(CardNotFoundException.class, () -> archive.getEgkByKvnr("123"));
  }

  @Test
  void shouldBeAbleToHaveCommentsOnCardConfig() {
    val archive = SmartcardArchive.fromResources();
    val egk = archive.getConfigsFor(SmartcardType.EGK).get(0);
    assertNotNull(egk.getNote());
    assertFalse(egk.getNote().isEmpty());
  }

  @Test
  void shouldCacheSmartcards() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards");
    val archive = SmartcardArchive.from(archiveFile.getAbsolutePath());

    val first = archive.getEgk(0);
    val second = archive.getEgkByICCSN(first.getIccsn());
    assertEquals(first, second);
  }

  @Test
  void shouldCacheSmartcardsByKvnr01() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards/smartcards.json");
    val archive = SmartcardArchive.from(archiveFile);

    val first = archive.getEgk(0);
    val second = archive.getEgkByKvnr(first.getKvnr());
    assertEquals(first, second);
  }

  @Test
  void shouldCacheSmartcardsByKvnr02() {
    val archiveFile = ResourceLoader.getFileFromResource("smartcards/smartcards.json");
    val archive = SmartcardArchive.from(archiveFile);

    val first = archive.getEgkByKvnr("X110406067");
    val second = archive.getEgkByKvnr(first.getKvnr());
    assertEquals(first, second);
  }

  @Test
  void shouldCacheSmartcardsByKvnr03() {
    resetArchiveCache();
    val archiveFile = ResourceLoader.getFileFromResource("smartcards/smartcards.json");
    val archive = SmartcardArchive.from(archiveFile);

    val first = archive.getEgkByKvnr("X110406067");
    val second = archive.getEgkByKvnr(first.getKvnr());
    assertEquals(first, second);
  }

  @Test
  void shouldGetConfigsFromCache() {
    val archive = SmartcardArchive.fromResources();
    assertFalse(archive.getConfigs().isEmpty());
  }

  @ParameterizedTest
  @MethodSource
  void shouldReadAllBundledEgks(ComplexLoadParameter param) {
    val archive = SmartcardArchive.fromResources();
    val fetcher = param.fetcher;
    val iccsns = param.iccsns;

    val smartcards = fetcher.apply(archive);
    assertEquals(smartcards.size(), iccsns.size());
    smartcards.forEach(s -> assertTrue(iccsns.contains(s.getIccsn())));
  }

  static Stream<Arguments> shouldReadAllBundledEgks() {
    return Stream.of(
            new ComplexLoadParameter(
                SmartcardArchive::getEgkCards,
                List.of("80276883110000113311", "80276883110000108142")),
            new ComplexLoadParameter(
                SmartcardArchive::getHbaCards, List.of("80276001011699901501")),
            new ComplexLoadParameter(
                SmartcardArchive::getSmcbCards,
                List.of("80276001011699901102", "80276001011699900861")))
        .map(Arguments::of);
  }

  @ParameterizedTest
  @EnumSource(
      value = SmartcardType.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"SMC_KT"})
  void shouldGetTypedConfigsFromCache(SmartcardType type) {
    val archive = SmartcardArchive.fromResources();
    assertFalse(archive.getConfigsFor(type).isEmpty());
  }

  @Test
  void shouldReadExtensionAsMapFromArchive() {
    val archive = SmartcardArchive.fromResources();
    val egkConfig =
        archive.getConfigsFor(SmartcardType.EGK).stream()
            .filter(c -> c.getIccsn().equals("80276883110000108142"))
            .findFirst()
            .orElseThrow();
    val ext = egkConfig.getSmartcardExtension();
    assertEquals("Hello", ext.get("first"));
    assertEquals("World", ext.get("second"));
    assertEquals(73, ext.get("num"));
  }

  @Test
  void shouldReadExtensionAsObject() {
    val archive = SmartcardArchive.fromResources();
    val egkConfig = archive.getByICCSN("80276883110000108142").orElseThrow();
    val ext = egkConfig.getExtensionAs(HelloWorldSmartcardExtension.class);
    assertEquals("Hello", ext.first);
    assertEquals("World", ext.second);
    assertEquals(73, ext.num);
  }

  private static class TestSmartcard extends SmartcardP12 {
    protected TestSmartcard(SmartcardConfigDto config) {
      super(SmartcardType.SMC_KT, config, List.of());
    }

    public static TestSmartcard forIccsn(String iccsn) {
      val config = new SmartcardConfigDto();
      config.setIccsn(iccsn);
      return new TestSmartcard(config);
    }

    @Override
    public List<Oid> getAutOids() {
      return List.of();
    }
  }

  private record ComplexLoadParameter(
      Function<SmartcardArchive, List<? extends Smartcard>> fetcher, List<String> iccsns) {}

  @Data
  private static class HelloWorldSmartcardExtension implements SmartcardExtension {
    private String first;
    private String second;
    private int num;
  }
}
