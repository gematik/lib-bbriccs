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

package de.gematik.bbriccs.rest.vau;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.vau.exceptions.VauException;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;
import kong.unirest.core.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class VauCertificateDownload {

  private VauCertificateDownload() {
    throw new IllegalAccessError("Utility class: don't use the constructor");
  }

  @SneakyThrows
  public static X509Certificate downloadFrom(String url, String xApiKey) {
    val certificateFactory = CertificateFactory.getInstance("X.509");

    val req = Unirest.get(url).header("X-api-key", xApiKey);
    val bytesBody = req.asBytes().getBody();
    val data =
        Optional.ofNullable(bytesBody)
            .filter(body -> body.length > 0)
            .orElseThrow(
                () -> new VauException(format("Remote on {0} returned an empty body", url)));
    log.info("Received VAU-Certificate successfully from {}", url);
    val byteArrayInputStream = new ByteArrayInputStream(data);

    try {
      return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
    } catch (CertificateException e) {
      throw new VauException("Error while generating VAU-Certificate from response", e);
    }
  }
}
