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

import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.cfg.SoftKonServiceConfiguration;
import de.gematik.bbriccs.konnektor.vsdm.VsdmService;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class SoftKonFactory implements KonnektorFactory {
  public static final String SERVICE_NAME = "Soft-Kon";

  @Override
  public String getType() {
    return SERVICE_NAME;
  }

  @Override
  public KonnektorBuildInstruction mapConfiguration(KonnektorConfiguration cfg) {
    val skscdto = cfg.getService().castTo(this, SoftKonServiceConfiguration.class);
    log.info("Build Konnektor-Client {} for {}", cfg.getName(), skscdto.getType());

    val smartcards = SmartcardArchive.from(skscdto.getSmartcards());
    val vsdmService =
        Optional.ofNullable(skscdto.getVsdmConfiguration())
            .map(VsdmService::createFrom)
            .orElseGet(VsdmService::instantiateWithTestKey);
    val service = new SofKonServicePort(smartcards, vsdmService);
    return KonnektorBuildInstruction.builder()
        .ctx(cfg.getContext().asContextType())
        .serviceProvider(service)
        .cardTerminals(List.of()) // SoftKon does not use any card terminals yet
        .build();
  }
}
