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

package de.gematik.bbriccs.popp

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import de.gematik.bbriccs.cfg.BaseConfigurationDto
import de.gematik.bbriccs.utils.ResourceLoader
import java.security.KeyStore

data class PoppTokenGeneratorConfiguration @JsonCreator constructor(
  @JsonProperty("keyStorePath")
  val keyStorePath: String,
  @JsonProperty("keyStorePassword")
  val keyStorePassword: CharArray,
  @JsonProperty("keyAlias")
  val keyAlias: String,
  @JsonProperty("kid")
  val kid: String,
  @JsonProperty("iss")
  val iss: String,
) : BaseConfigurationDto {
  fun loadKeyStore(type: String = KeyStore.getDefaultType()): KeyStore =
    KeyStore.getInstance(type).apply {
      ResourceLoader.getFileFromResourceAsStream(keyStorePath).use { input ->
        load(input, keyStorePassword)
      }
    }
}
