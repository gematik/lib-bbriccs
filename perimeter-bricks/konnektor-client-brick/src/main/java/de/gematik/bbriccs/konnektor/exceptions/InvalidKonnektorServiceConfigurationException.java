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

package de.gematik.bbriccs.konnektor.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.konnektor.KonnektorFactory;

public class InvalidKonnektorServiceConfigurationException extends RuntimeException {

  public InvalidKonnektorServiceConfigurationException(
      KonnektorFactory serviceFactory, Class<?> configurationClass, String configurationName) {
    super(
        format(
            "{0} {1} was provided a configuration of type {2} which is intended to be"
                + " used with {3}",
            serviceFactory.getClass().getSimpleName(),
            serviceFactory.getType(),
            configurationClass.getSimpleName(),
            configurationName));
  }
}
