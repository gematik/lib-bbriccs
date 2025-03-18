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

package de.gematik.bbriccs.cardterminal.cfg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gematik.bbriccs.cardterminal.CardTerminalFactoryService;
import de.gematik.bbriccs.cardterminal.exceptions.InvalidCardTerminalConfigurationException;
import de.gematik.bbriccs.cfg.ExtendableConfigurationElement;
import lombok.Data;

@Data
public abstract class CardTerminalConfiguration implements ExtendableConfigurationElement {

  private String type;

  @JsonIgnore
  public <T extends CardTerminalConfiguration> T castTo(
      CardTerminalFactoryService ctsf, Class<T> clazz) {
    try {
      return clazz.cast(this);
    } catch (ClassCastException cce) {
      throw new InvalidCardTerminalConfigurationException(ctsf, this.getClass(), this.getType());
    }
  }
}
