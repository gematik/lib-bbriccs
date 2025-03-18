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

package de.gematik.bbriccs.cfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ConfigurationReader {

  private ConfigurationReader() {
    throw new IllegalAccessError("not to be used yet!");
  }

  public static ObjectMapper getConfigurationMapper() {
    val mapper = new ObjectMapper(new YAMLFactory());

    val loader = ServiceLoader.load(ConfigurationSubTypeProvider.class);
    loader.stream()
        .map(ServiceLoader.Provider::get)
        .forEach(
            csts -> {
              log.info("Register Configuration-SubType {}", csts.getSubType().getSimpleName());
              mapper.registerSubtypes(new NamedType(csts.getSubType(), csts.getSubTypeName()));
            });

    return mapper;
  }
}
