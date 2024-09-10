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

import de.gematik.bbriccs.cardterminal.CardTerminalOperator;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.exceptions.MissingKonnektorServiceException;
import java.util.Optional;
import java.util.ServiceLoader;
import lombok.val;

public interface Konnektor {
  org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Konnektor.class);

  String getName();

  <R> KonnektorResponse<R> execute(KonnektorRequest<R> cmd);

  <R> Optional<KonnektorResponse<R>> executeSafely(KonnektorRequest<R> cmd);

  CardTerminalOperator getCardTerminalOperator();

  static Konnektor create(KonnektorConfiguration cfg) {
    val serviceCfg = cfg.getService();
    val serviceFactory = loadKonnektorService(serviceCfg.getType());
    val kbi = serviceFactory.mapConfiguration(cfg);
    return new KonnektorImpl(kbi.getCtx(), kbi.getServiceProvider(), kbi.getCardTerminals());
  }

  private static KonnektorFactory loadKonnektorService(String named) {
    val loader = ServiceLoader.load(KonnektorFactory.class);
    log.info("Found Konnektor-Services:");
    loader.forEach(ksf -> log.info(ksf.getType()));
    return loader.stream()
        .map(ServiceLoader.Provider::get)
        .filter(ksf -> ksf.getType().equalsIgnoreCase(named))
        .findFirst()
        .orElseThrow(() -> new MissingKonnektorServiceException(named));
  }
}
