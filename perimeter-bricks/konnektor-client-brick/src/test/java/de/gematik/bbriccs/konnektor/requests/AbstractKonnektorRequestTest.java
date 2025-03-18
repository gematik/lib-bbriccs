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

package de.gematik.bbriccs.konnektor.requests;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.konnektor.ServicePort;
import de.gematik.bbriccs.konnektor.exceptions.SOAPRequestException;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;

class AbstractKonnektorRequestTest {

  @Test
  void shouldExecuteSupplier() {
    val request = new TestSupplierKonnektorRequest(false);
    val result = request.execute(null, null);
    assertEquals("okay", result);
  }

  @Test
  void shouldCatchOnExecuteSupplier() {
    val request = new TestSupplierKonnektorRequest(true);
    assertThrows(SOAPRequestException.class, () -> request.execute(null, null));
  }

  @Test
  void shouldExecuteAction() {
    val request = new TestActionKonnektorRequest(false);
    val result = request.execute(null, null);
    assertEquals("okay", result);
  }

  @Test
  void shouldCatchOnExecuteAction() {
    val request = new TestActionKonnektorRequest(true);
    assertThrows(SOAPRequestException.class, () -> request.execute(null, null));
  }

  @RequiredArgsConstructor
  private static class TestSupplierKonnektorRequest extends AbstractKonnektorRequest<String> {
    private final boolean showThrow;

    @Override
    public String execute(ContextType ctx, ServicePort serviceProvider) {
      if (showThrow) return this.executeSupplier(null);
      else return this.executeSupplier(() -> "okay");
    }
  }

  @RequiredArgsConstructor
  private static class TestActionKonnektorRequest extends AbstractKonnektorRequest<String> {
    private final boolean showThrow;
    private final StringBuilder input = new StringBuilder();

    @Override
    public String execute(ContextType ctx, ServicePort serviceProvider) {
      if (showThrow) this.executeAction(null);
      else this.executeAction(() -> input.append("okay"));
      return input.toString();
    }
  }
}
