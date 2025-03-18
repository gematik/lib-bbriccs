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

package de.gematik.bbriccs.smartcards.cli.cmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.cli.param.InputOutputDirectoryParameter;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardOwnerData;
import de.gematik.bbriccs.smartcards.SmartcardType;
import java.util.Base64;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(
    name = "adv",
    description = "Transform Smartcards Archive to AdV-Smartcards",
    mixinStandardHelpOptions = true)
@Slf4j
public class AdvSmartcards implements Callable<Integer> {

  @CommandLine.Mixin protected InputOutputDirectoryParameter inputOutputDirectory;

  @Override
  public Integer call() throws Exception {
    val sca = SmartcardArchive.from(inputOutputDirectory.getInputPath().toFile());
    val vegks =
        sca.getICCSNsFor(SmartcardType.EGK).stream()
            .map(
                iccsn -> {
                  val smartcard = sca.getEgkByICCSN(iccsn);
                  return transformTo(smartcard);
                })
            .toList();

    val om = new ObjectMapper();
    val out = om.writerWithDefaultPrettyPrinter().writeValueAsString(vegks);
    System.out.println(out);
    return 0;
  }

  @SneakyThrows
  private VEgk transformTo(Egk egk) {
    val vegk = new VEgk();
    vegk.setIccsn(egk.getIccsn());
    vegk.setKvnr(egk.getKvnr());
    vegk.setOwner(VOwnerData.from(egk.getOwnerData()));

    val crypto = new VCryptoData();
    crypto.setPrivateKey(egk.getPrivateKeyBase64());
    crypto.setCertificate(
        Base64.getEncoder()
            .encodeToString(egk.getAutCertificate().getX509Certificate().getEncoded()));

    vegk.setCrypto(crypto);
    return vegk;
  }

  @Data
  private static class VEgk {
    private String iccsn;
    private String kvnr;
    private VOwnerData owner;
    private VCryptoData crypto;
  }

  @Data
  private static class VOwnerData {
    private String firstName;
    private String lastName;

    public static VOwnerData from(SmartcardOwnerData sod) {
      val vod = new VOwnerData();
      val nameTokens = sod.getCommonName().replace("TEST-ONLY", "").split(" ");

      val firstName = nameTokens[0];
      val lastName = nameTokens[nameTokens.length - 1];
      vod.setFirstName(firstName);
      vod.setLastName(lastName);
      return vod;
    }
  }

  @Data
  private static class VCryptoData {
    private String privateKey;
    private String certificate;
  }
}
