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

import static java.text.MessageFormat.*;

import de.gematik.bbriccs.cardterminal.CardTerminal;
import de.gematik.bbriccs.cardterminal.CardTerminalOperator;
import de.gematik.ws.conn.connectorcontext.v2.*;
import java.time.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
public final class KonnektorImpl implements Konnektor {

  private final ServicePort serviceProvider;
  private final ContextType ctx;
  @Getter private final CardTerminalOperator cardTerminalOperator;

  public KonnektorImpl(
      ContextType ctx, ServicePort serviceProvider, Collection<CardTerminal> cardTerminals) {
    this.ctx = ctx;
    this.serviceProvider = serviceProvider;
    this.cardTerminalOperator = new CardTerminalOperator(cardTerminals);
  }

  @Override
  public String getName() {
    return this.serviceProvider.getSds().getProductName();
  }

  @Override
  public <R> KonnektorResponse<R> execute(KonnektorRequest<R> cmd) {
    log.info("Execute {} on {}", cmd.getClass().getSimpleName(), this);
    val start = Instant.now();
    val response = cmd.execute(ctx, serviceProvider);
    val duration = Duration.between(start, Instant.now());
    log.info(
        format(
            "Received Response for {0} from {1} within {2}",
            cmd.getClass().getSimpleName(), this, duration.toMillis()));
    return new KonnektorResponse<>(response, duration);
  }

  @Override
  public <R> Optional<KonnektorResponse<R>> executeSafely(KonnektorRequest<R> cmd) {
    try {
      return Optional.of(execute(cmd));
    } catch (Exception e) {
      log.warn(
          format(
              "Execute {0} produced an error: {1}",
              cmd.getClass().getSimpleName(), e.getMessage()));
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    val ctxString =
        format(
            "ctx=[clientSystem={0}, mandant={1}, wp={2}, user={3}]",
            ctx.getClientSystemId(), ctx.getMandantId(), ctx.getWorkplaceId(), ctx.getUserId());
    return format("Konnektor \"{0}\" with {1}", this.getName(), ctxString);
  }
}
