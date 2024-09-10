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

package de.gematik.bbriccs.smartcards.cfg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.gematik.bbriccs.smartcards.SmartcardType;
import java.util.*;
import lombok.*;

@Data
@JsonInclude(Include.NON_EMPTY)
public class SmartcardConfigDto {

  private String iccsn;
  private String identifier;
  private SmartcardType type;
  private String ownerName;
  private List<String> stores;
  private String note;
  private Map<String, Object> smartcardExtension = Map.of();

  public String getNote() {
    return Optional.ofNullable(note).orElse("");
  }
}
