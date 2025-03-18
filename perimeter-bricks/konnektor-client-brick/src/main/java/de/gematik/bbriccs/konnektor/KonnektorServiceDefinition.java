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

import de.gematik.ws.conn.servicedirectory.ConnectorServices;
import de.gematik.ws.conn.servicedirectory.VersionType;
import java.lang.module.ModuleDescriptor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Getter
@Builder
@Slf4j
public final class KonnektorServiceDefinition {

  private final String productName;
  private final String signatureService;
  private final String authSignatureService;
  private final String certificateService;
  private final String eventService;
  private final String cardService;
  private final String cardTerminalService;
  private final String vsdService;
  private final String encryptionService;

  public static KonnektorServiceDefinition from(ConnectorServices cs) {
    val ksdb = KonnektorServiceDefinition.builder();

    ksdb.productName(cs.getProductInformation().getProductMiscellaneous().getProductName());

    val serviceInfo = cs.getServiceInformation();
    serviceInfo
        .getService()
        .forEach(
            service -> {
              // Note: take always the first/latest version and TLS for now!
              val version =
                  service.getVersions().getVersion().stream()
                      .sorted(KonnektorServiceDefinition::compareVersions)
                      .toList()
                      .get(0);
              val endpointLocation = version.getEndpointTLS().getLocation();
              switch (service.getName().toLowerCase()) {
                case "cardservice":
                  ksdb.cardService(endpointLocation);
                  break;
                case "certificateservice":
                  ksdb.certificateService(endpointLocation);
                  break;
                case "eventservice":
                  ksdb.eventService(endpointLocation);
                  break;
                case "encryptionservice":
                  ksdb.encryptionService(endpointLocation);
                  break;
                case "cardterminalservice":
                  ksdb.cardTerminalService(endpointLocation);
                  break;
                case "authsignatureservice":
                  ksdb.authSignatureService(endpointLocation);
                  break;
                case "signatureservice":
                  ksdb.signatureService(endpointLocation);
                  break;
                case "vsdservice":
                  ksdb.vsdService(endpointLocation);
                  break;
                default:
                  log.info(
                      "Konnektor providing service {} ({}) which is not mapped to {}",
                      service.getName(),
                      endpointLocation,
                      KonnektorServiceDefinition.class.getSimpleName());
              }
            });

    return ksdb.build();
  }

  private static int compareVersions(VersionType v1, VersionType v2) {
    val jV1 = ModuleDescriptor.Version.parse(v1.getVersion());
    val jV2 = ModuleDescriptor.Version.parse(v2.getVersion());
    return jV2.compareTo(jV1);
  }

  public static KonnektorServiceDefinition forSoftKon() {
    return KonnektorServiceDefinition.builder()
        .productName("Soft-Konnektor")
        .signatureService("mock/signatureservice")
        .authSignatureService("mock/authsignatureservice")
        .certificateService("mock/certificateservice")
        .eventService("mock/eventservice")
        .cardService("mock/cardservice")
        .cardTerminalService("mock/cardterminalservice")
        .vsdService("mock/vsdservice")
        .encryptionService("mock/encryptionservice")
        .build();
  }
}
