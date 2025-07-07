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

import de.gematik.bbriccs.crypto.BC;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import de.gematik.bbriccs.utils.ResourceLoader;
import jakarta.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The SmartcardArchive is a factory for Smartcards, which is used to load, cache and manage
 * Smartcards from a given directory.
 */
@Slf4j
public class SmartcardArchive {

  private static final Map<File, SmartcardArchive> ARCHIVE_CACHE = new HashMap<>();
  @Nullable private static SmartcardArchive defaultArchive = null;

  static {
    BC.init();
  }

  private final SmartcardFactory factory;
  private final Map<String, SmcB> smcbCache;
  private final Map<String, Hba> hbaCache;
  private final Map<String, Egk> egkCache;

  private SmartcardArchive(SmartcardFactory factory) {
    this.factory = factory;
    this.smcbCache = new HashMap<>();
    this.hbaCache = new HashMap<>();
    this.egkCache = new HashMap<>();
  }

  /**
   * Initialize the {@link SmartcardArchive} from resources. For this method to work the {@link
   * ResourceLoader} must be able to load the smartcards index file {@code
   * resources/smartcards/smartcards.json}
   *
   * @return the SmartcardArchive initialized with the {@code smartcards.json} file from resources
   */
  public static SmartcardArchive fromResources() {
    if (defaultArchive == null) {
      val factory = SmartcardFactory.fromResources();
      defaultArchive = new SmartcardArchive(factory);
    }
    return defaultArchive;
  }

  /**
   * Initialize the {@link SmartcardArchive} with a smartcards index file from a given path.
   *
   * @param path the path to the smartcards index file or the directory containing the {@code
   *     smartcards.json} file
   * @return the SmartcardArchive initialized with the given smartcards index file
   */
  public static SmartcardArchive from(String path) {
    return from(Path.of(path).toFile());
  }

  /**
   * Initialize the {@link SmartcardArchive} with a smartcards index file from a given path.
   *
   * @param archiveFile pointing to the smartcards.json file or the directory containing the
   *     smartcards.json file
   * @return the {@link SmartcardArchive} initialized with the smartcards.json file from the given
   *     File
   */
  public static SmartcardArchive from(File archiveFile) {
    return ARCHIVE_CACHE.computeIfAbsent(
        archiveFile,
        file -> {
          val factory = SmartcardFactory.fromFileSystem(file);
          return new SmartcardArchive(factory);
        });
  }

  /**
   * Get all Smartcard configurations known to this {@link SmartcardArchive} from the smartcards
   * index file.
   *
   * @return a list of all known Smartcard configurations
   */
  public List<SmartcardConfigDto> getConfigs() {
    return this.factory.getConfigs();
  }

  /**
   * Get all Smartcard configurations known to this {@link SmartcardArchive} for a specific {@link
   * SmartcardType}.
   *
   * @param type of Smartcards
   * @return a list of all known Smartcard configurations for the given {@link SmartcardType}
   */
  public List<SmartcardConfigDto> getConfigsFor(SmartcardType type) {
    return this.factory.getConfigsFor(type);
  }

  /**
   * Get all the ICCSNs of the Smartcards of a specific {@link SmartcardType} known to this {@link
   * SmartcardArchive}
   *
   * @param type of Smartcards
   * @return a list of all ICCSNs of the Smartcards of the given {@link SmartcardType}
   */
  public List<String> getICCSNsFor(SmartcardType type) {
    return this.factory.getConfigsFor(type).stream().map(SmartcardConfigDto::getIccsn).toList();
  }

  /**
   * Get a {@link Smartcard} with {@code classType} and identified by its ICCSN.
   *
   * @param classType of the Smartcard
   * @param iccsn of the Smartcard
   * @param <T> the type of Smartcard
   * @return the Smartcard identified by the given ICCSN
   */
  public <T extends Smartcard> T getByICCSN(Class<T> classType, String iccsn) {
    if (SmcB.class.isAssignableFrom(classType)) {
      return classType.cast(this.getSmcbByICCSN(iccsn));
    } else if (Hba.class.isAssignableFrom(classType)) {
      return classType.cast(this.getHbaByICCSN(iccsn));
    } else if (Egk.class.isAssignableFrom(classType)) {
      return classType.cast(this.getEgkByICCSN(iccsn));
    } else if (InstituteSmartcard.class.isAssignableFrom(classType)) {
      return classType.cast(
          this.getByICCSN(iccsn).orElseThrow(() -> new CardNotFoundException(classType, iccsn)));
    } else {
      val smartcard =
          this.getByICCSN(iccsn).orElseThrow(() -> new CardNotFoundException(classType, iccsn));
      try {
        return classType.cast(smartcard);
      } catch (ClassCastException cce) {
        log.error(format("Unable to cast Smartcard {0}", smartcard.getIccsn()), cce);
        throw new InvalidSmartcardTypeException(classType.getSimpleName());
      }
    }
  }

  /**
   * Get a {@link Smartcard} with {@link SmartcardType} {@code type} and identified by its ICCSN.
   *
   * @param type of the Smartcard
   * @param iccsn of the Smartcard
   * @return the Smartcard identified by the given ICCSN
   */
  public Smartcard getSmartcardByICCSN(SmartcardType type, String iccsn) {
    return switch (type) {
      case SMC_B -> this.getSmcbByICCSN(iccsn);
      case HBA -> this.getHbaByICCSN(iccsn);
      case EGK -> this.getEgkByICCSN(iccsn);
      default -> throw new InvalidSmartcardTypeException(type);
    };
  }

  /**
   * Get a {@link Smartcard} safely identified by its ICCSN.
   *
   * @param iccsn of the Smartcard
   * @return {@link Optional} of the {@link Smartcard} if the ICCSN is known to this {@link
   *     SmartcardArchive} or an empty {@link Optional} otherwise
   */
  public Optional<Smartcard> getByICCSN(String iccsn) {
    return this.getConfigs().stream()
        .filter(cfg -> cfg.getIccsn().equals(iccsn))
        .findFirst()
        .map(cit -> this.getSmartcardByICCSN(cit.getType(), iccsn));
  }

  /**
   * Get a {@link Hba} from this {@link SmartcardArchive} identified by its index.
   *
   * @param idx of the {@link Hba}
   * @return a {@link Hba} from this {@link SmartcardArchive} identified by its index.
   */
  public Hba getHba(int idx) {
    val hbaConfig = this.factory.getConfigFor(SmartcardType.HBA, idx);
    return this.getHbaByICCSN(hbaConfig.getIccsn());
  }

  /**
   * Get a {@link Hba} from this {@link SmartcardArchive} identified by its ICCSN.
   *
   * @param iccsn of the {@link Hba}
   * @return the {@link Hba} with the given ICCSN
   */
  public Hba getHbaByICCSN(String iccsn) {
    return this.hbaCache.computeIfAbsent(
        iccsn, key -> factory.loadSmartcardByIccsn(HbaP12.class, iccsn));
  }

  /**
   * Get all {@link Hba} Smartcards which are configured in this {@link SmartcardArchive}
   *
   * @return a list of all {@link Hba} Smartcards
   */
  public List<Hba> getHbaCards() {
    return this.getConfigsFor(SmartcardType.HBA).stream()
        .map(c -> this.getHbaByICCSN(c.getIccsn()))
        .toList();
  }

  /**
   * Get a {@link SmcB} from this {@link SmartcardArchive} identified by its index.
   *
   * @param idx of the {@link SmcB}
   * @return a {@link SmcB} from this {@link SmartcardArchive} identified by its index.
   */
  public SmcB getSmcB(int idx) {
    val smcbConfig = this.factory.getConfigFor(SmartcardType.SMC_B, idx);
    return this.getSmcbByICCSN(smcbConfig.getIccsn());
  }

  /**
   * Get a {@link SmcB} from this {@link SmartcardArchive} identified by its ICCSN.
   *
   * @param iccsn of the {@link SmcB}
   * @return the {@link SmcB} with the given ICCSN
   */
  public SmcB getSmcbByICCSN(String iccsn) {
    return this.smcbCache.computeIfAbsent(
        iccsn, key -> factory.loadSmartcardByIccsn(SmcBP12.class, iccsn));
  }

  /**
   * Get all {@link SmcB} Smartcards which are configured in this {@link SmartcardArchive}
   *
   * @return a list of all {@link SmcB} Smartcards
   */
  public List<SmcB> getSmcbCards() {
    return this.getConfigsFor(SmartcardType.SMC_B).stream()
        .map(c -> this.getSmcbByICCSN(c.getIccsn()))
        .toList();
  }

  /**
   * Get a {@link Egk} from this {@link SmartcardArchive} identified by its index.
   *
   * @param idx of the {@link Egk}
   * @return a {@link Egk} from this {@link SmartcardArchive} identified by its index.
   */
  public Egk getEgk(int idx) {
    val egkConfig = this.factory.getConfigFor(SmartcardType.EGK, idx);
    return this.getEgkByICCSN(egkConfig.getIccsn());
  }

  /**
   * Get a {@link Egk} from this {@link SmartcardArchive} identified by its ICCSN.
   *
   * @param iccsn of the {@link Egk}
   * @return the {@link Egk} with the given ICCSN
   */
  public Egk getEgkByICCSN(String iccsn) {
    return this.egkCache.computeIfAbsent(
        iccsn, key -> factory.loadSmartcardByIccsn(EgkP12.class, iccsn));
  }

  /**
   * Get a {@link Egk} from this {@link SmartcardArchive} identified by its KVNR.
   *
   * @param kvnr of the {@link Egk}
   * @return the {@link Egk} with the given KVNR
   */
  public Egk getEgkByKvnr(String kvnr) {
    return this.egkCache.values().stream()
        .filter(egk -> egk.getKvnr().equals(kvnr))
        .findFirst()
        .orElseGet(
            () -> {
              val egk = this.factory.loadEgkByKvnr(kvnr);
              this.egkCache.put(egk.getIccsn(), egk);
              return egk;
            });
  }

  /**
   * Get all {@link Egk} Smartcards which are configured in this {@link SmartcardArchive}
   *
   * @return a list of all {@link Egk} Smartcards
   */
  public List<Egk> getEgkCards() {
    return this.getConfigsFor(SmartcardType.EGK).stream()
        .map(c -> this.getEgkByICCSN(c.getIccsn()))
        .toList();
  }
}
