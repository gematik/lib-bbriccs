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

package de.gematik.bbriccs.utils

import de.gematik.bbriccs.crypto.CryptographySpecification

interface TrustedEnvironmentAnchor {
  fun getCaDownloadPath(type: CaType = CaType.ROOT_CA, algorithm: CryptographySpecification = CryptographySpecification.ECC): String
  fun getUrl(useInternet: Boolean = true): String
}
enum class TiTrustedEnvironmentAnchor(private val internet: String, private val ti: String) : TrustedEnvironmentAnchor {
  TU("https://download-test.tsl.ti-dienste.de", "http://download-test.tsl.telematik-test"),
  RU("https://download-ref.tsl.ti-dienste.de", "http://download-ref.tsl.telematik-test"),
  PU("https://download.tsl.ti-dienste.de", "http://download.tsl.telematik"),
  ;

  override fun getCaDownloadPath(
    type: CaType,
    algorithm: CryptographySpecification,
  ): String {
    return when (type) {
      CaType.ROOT_CA -> "/${algorithm.name.uppercase()}/ROOT-CA/roots.json"
      CaType.SUB_CA -> "/${algorithm.name.uppercase()}/SUB-CA/"
    }
  }

  override fun getUrl(useInternet: Boolean) = when {
    useInternet -> internet
    else -> ti
  }
}

enum class CaType {
  ROOT_CA,
  SUB_CA,
  ;

  override fun toString() = this.name.replace("_", "-").uppercase()
}
