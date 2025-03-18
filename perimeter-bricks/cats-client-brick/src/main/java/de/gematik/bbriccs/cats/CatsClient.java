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

package de.gematik.bbriccs.cats;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.gematik.bbriccs.cardterminal.CardTerminal;
import de.gematik.bbriccs.cardterminal.CardTerminalSlot;
import de.gematik.bbriccs.cardterminal.exceptions.CardTerminalException;
import de.gematik.bbriccs.cats.dto.CardConfigurationDto;
import de.gematik.bbriccs.cats.dto.CardStatusDto;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.bbriccs.rest.RestClient;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.smartcards.Smartcard;
import java.util.*;
import java.util.concurrent.TimeUnit;
import kong.unirest.core.MimeTypes;
import lombok.*;

public class CatsClient implements CardTerminal {

  private static final int CATS_DEFAULT_SLOT_COUNT = 4;

  @Getter private final String ctId;
  private final String configPath;
  private final ObjectMapper mapper;
  private final HttpBClient restClient;

  private final Map<Integer, CardTerminalSlot> slots = new HashMap<>(CATS_DEFAULT_SLOT_COUNT);

  private CatsClient(String ctId, String configPath, HttpBClient restClient) {
    this.mapper = new JsonMapper();
    this.ctId = ctId;
    this.configPath = configPath;
    this.restClient = restClient;
  }

  public static Builder create(String address) {
    return create(
        RestClient.forUrl(address)
            .withHeader(StandardHttpHeaderKey.CONTENT_TYPE.createHeader(MimeTypes.JSON))
            .withHeader(StandardHttpHeaderKey.ACCEPT.createHeader(MimeTypes.JSON))
            .withoutTlsVerification()
            .init());
  }

  public static Builder create(HttpBClient restClient) {
    return new Builder(restClient);
  }

  @Override
  public CardTerminal connect() {
    for (int i = 0; i < CATS_DEFAULT_SLOT_COUNT; i++) {
      this.slots.put(i, new CardTerminalSlot(i));
    }
    return this;
  }

  @SneakyThrows
  @SuppressWarnings("java:S1192") // not an issue here!
  @Override
  public void insertCard(Smartcard card, int slotId) {
    if (!this.slots.containsKey(slotId))
      throw new CardTerminalException(
          format("Slot {0} does not exist on CATS {1}", slotId, this.ctId));

    // deactivate card
    request("/config/card/insert", new CardStatusDto(slotId, false));
    this.slots.get(slotId).remove();

    // change card configuration
    val cardType = card.getType().name().toLowerCase().replace("-", "_");
    request(
        "/config/card/configuration",
        new CardConfigurationDto(slotId, toCatsConfigurationPath(cardType, card.getIccsn())));

    // activate card
    request("/config/card/insert", new CardStatusDto(slotId, true));
    this.slots.get(slotId).inserte(card.getIccsn());

    // well, CATS requires some time until the card is inserted??
    TimeUnit.SECONDS.sleep(1);
  }

  @Override
  public void insertCard(Smartcard card) {
    val freeSlot =
        getFreeSlot().orElseThrow(() -> new CardTerminalException("No free slot available"));
    insertCard(card, freeSlot.getSlotId());
  }

  @Override
  public void resetSlots() {
    this.slots
        .values()
        .forEach(
            slot -> {
              request("/config/card/insert", new CardStatusDto(slot.getSlotId(), false));
              slot.remove();
            });
  }

  @Override
  public Optional<CardTerminalSlot> getFreeSlot() {
    return this.slots.values().stream().filter(CardTerminalSlot::isFree).findFirst();
  }

  @SneakyThrows
  private <T> void request(String path, T body) {
    val bodyString = this.mapper.writeValueAsString(body);
    val req = new HttpBRequest(HttpRequestMethod.POST, path, bodyString);
    val resp = this.restClient.send(req);

    if (resp.statusCode() != 200) {
      val operation = format("{0} {1}", HttpRequestMethod.POST, path);
      throw new CardTerminalException(operation, resp.statusCode(), resp.bodyAsString());
    }
  }

  private String toCatsConfigurationPath(String cardType, String iccsn) {
    return format("{0}/configuration_{1}_{2}.xml", this.configPath, cardType, iccsn);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final HttpBClient restClient;
    private String configPath;

    public Builder configPath(String configPath) {
      this.configPath = configPath;
      return this;
    }

    public CatsClient withTerminalId(String ctId) {
      // when no config path is set, use default for CATS running in Docker-Container
      this.configPath =
          Objects.requireNonNullElse(
              this.configPath,
              "/opt/cats-configuration/card-simulation/CardSimulationConfigurations");
      return new CatsClient(ctId, configPath, restClient);
    }
  }
}
