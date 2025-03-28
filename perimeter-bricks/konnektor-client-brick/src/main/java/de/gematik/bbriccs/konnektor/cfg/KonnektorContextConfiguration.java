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

package de.gematik.bbriccs.konnektor.cfg;

import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import lombok.Data;
import lombok.val;

@Data
public class KonnektorContextConfiguration {

  private String mandantId;
  private String clientSystemId;
  private String workplaceId;
  private String userId;

  public ContextType asContextType() {
    val ctx = new ContextType();
    ctx.setMandantId(mandantId);
    ctx.setClientSystemId(clientSystemId);
    ctx.setWorkplaceId(workplaceId);
    ctx.setUserId(userId);
    return ctx;
  }

  public static KonnektorContextConfiguration getDefaultContextConfiguration() {
    val ctx = new KonnektorContextConfiguration();
    ctx.setMandantId("Mandant1");
    ctx.setClientSystemId("CS1");
    ctx.setWorkplaceId("WP1");
    ctx.setUserId("User1");
    return ctx;
  }

  public static ContextType getDefaultContextType() {
    val ctx = new ContextType();
    ctx.setMandantId("Mandant1");
    ctx.setClientSystemId("CS1");
    ctx.setWorkplaceId("WP1");
    return ctx;
  }
}
