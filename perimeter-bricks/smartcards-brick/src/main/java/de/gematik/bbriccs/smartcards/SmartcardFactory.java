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

package de.gematik.bbriccs.smartcards;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.bbriccs.smartcards.exceptions.SmartcardFactoryException;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
class SmartcardFactory {

  private static final String DEFAULT_INDEX_DIR = "smartcards";
  private static final String DEFAULT_INDEX_FILE = "smartcards.json";

  private final Function<String, InputStream> fileLoader;

  @Getter private final List<SmartcardConfigDto> configs;

  private SmartcardFactory(
      List<SmartcardConfigDto> configs, Function<String, InputStream> fileLoader) {
    this.fileLoader = fileLoader;
    this.configs = configs;
  }

  public List<SmartcardConfigDto> getConfigsFor(SmartcardType type) {
    return this.configs.stream().filter(config -> config.getType().equals(type)).toList();
  }

  public SmartcardConfigDto getConfigFor(SmartcardType type, int idx) {
    return this.getConfigsFor(type).get(idx);
  }

  @SneakyThrows
  public <S extends SmartcardP12> S loadSmartcardByIccsn(Class<S> smartcardType, String iccsn) {
    val config = loadSmartcardConfig(SmartcardType.fromImplementationType(smartcardType), iccsn);
    val constructor = smartcardType.getConstructor(SmartcardConfigDto.class, List.class);

    val certificates =
        config.getStores().stream()
            .map(
                path -> {
                  log.trace("Load Smartcard certificate from Store={}", path);
                  return new SmartcardCertificateP12(path, () -> this.fileLoader.apply(path));
                })
            .toList();

    return constructor.newInstance(config, certificates);
  }

  public Egk loadEgkByKvnr(String kvnr) {
    return this.configs.stream()
        .filter(it -> it.getType() == SmartcardType.EGK)
        .filter(it -> it.getIdentifier().equals(kvnr))
        .findFirst()
        .map(it -> loadSmartcardByIccsn(EgkP12.class, it.getIccsn()))
        .orElseThrow(() -> new CardNotFoundException(SmartcardType.EGK, kvnr));
  }

  private SmartcardConfigDto loadSmartcardConfig(SmartcardType smartcardType, String iccsn) {
    return this.configs.stream()
        .filter(it -> smartcardType.equals(it.getType()))
        .filter(it -> it.getIccsn().equals(iccsn))
        .findFirst()
        .orElseThrow(() -> new CardNotFoundException(smartcardType, iccsn));
  }

  private static List<SmartcardConfigDto> loadConfigs(
      Path basePath, Supplier<InputStream> indexFile) {
    try (val is = indexFile.get()) {
      return new ObjectMapper().readValue(is, new TypeReference<>() {});
    } catch (IOException e) {
      throw new SmartcardFactoryException(
          format("Unable to load Smartcard-Index from {0}", basePath), e);
    }
  }

  public static SmartcardFactory fromFileSystem(File input) {
    val indexFile = (input.isFile()) ? input : new File(input, DEFAULT_INDEX_FILE);
    val basePath = indexFile.getParentFile().toPath();

    Function<String, InputStream> fileLoader =
        path -> {
          try {
            return Files.newInputStream(basePath.resolve(path));
          } catch (IOException e) {
            throw new SmartcardFactoryException(
                format("Unable to load File from {0}", basePath.resolve(path)), e);
          }
        };

    Supplier<InputStream> indexSupplier = () -> fileLoader.apply(indexFile.getName());
    val configs = loadConfigs(basePath, indexSupplier);

    return new SmartcardFactory(configs, fileLoader);
  }

  /**
   * Load SmartcardFactory from resources
   *
   * @return a new SmartcardFactory instance
   */
  public static SmartcardFactory fromResources() {
    Function<String, InputStream> fileLoader =
        path ->
            ResourceLoader.getFileFromResourceAsStream(format("{0}/{1}", DEFAULT_INDEX_DIR, path));

    Supplier<InputStream> indexSupplier = () -> fileLoader.apply(DEFAULT_INDEX_FILE);
    val configs = loadConfigs(Path.of(DEFAULT_INDEX_FILE), indexSupplier);

    return new SmartcardFactory(configs, fileLoader);
  }
}
