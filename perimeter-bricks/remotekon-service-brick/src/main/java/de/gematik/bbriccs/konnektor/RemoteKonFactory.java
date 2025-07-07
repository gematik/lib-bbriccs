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

import de.gematik.bbriccs.cardterminal.CardTerminalFactory;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.cfg.RemoteKonServiceConfiguration;
import java.net.URL;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RemoteKonFactory implements KonnektorFactory {
  public static final String SERVICE_NAME = "Remote-Kon";

  @Override
  public String getType() {
    return SERVICE_NAME;
  }

  @SneakyThrows
  @Override
  public KonnektorBuildInstruction mapConfiguration(KonnektorConfiguration cfg) {
    val rkscdto = cfg.getService().castTo(this, RemoteKonServiceConfiguration.class);
    log.info("Build Konnektor-Client {} for {}", cfg.getName(), rkscdto.getType());
    log.info("Connect to {} on {}", rkscdto.getType(), rkscdto.getAddress());

    val url = new URL(rkscdto.getAddress());
    val service =
        RemoteKonServicePort.onRemote(url)
            .tls(rkscdto.getTls())
            .auth(rkscdto.getBasicAuth())
            .build();
    val cardTerminals = cfg.getCardTerminals().stream().map(CardTerminalFactory::create).toList();
    return KonnektorBuildInstruction.builder()
        .ctx(cfg.getContext().asContextType())
        .serviceProvider(service)
        .cardTerminals(cardTerminals)
        .build();
  }
}
