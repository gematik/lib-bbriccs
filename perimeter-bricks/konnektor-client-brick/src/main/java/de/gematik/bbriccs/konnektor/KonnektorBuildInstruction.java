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

import de.gematik.bbriccs.cardterminal.CardTerminal;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class KonnektorBuildInstruction {

  private final ContextType ctx;
  private final ServicePort serviceProvider;
  private final Collection<CardTerminal> cardTerminals;
}
