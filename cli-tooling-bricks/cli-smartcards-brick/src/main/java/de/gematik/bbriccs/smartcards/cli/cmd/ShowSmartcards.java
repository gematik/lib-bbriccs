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

import static java.text.MessageFormat.format;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;

import de.gematik.bbriccs.cli.param.InputDirectoryParameter;
import de.gematik.bbriccs.smartcards.*;
import de.gematik.bbriccs.smartcards.exceptions.InvalidSmartcardTypeException;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Command(name = "show", description = "Show Smartcards Archive", mixinStandardHelpOptions = true)
@Slf4j
public class ShowSmartcards implements Callable<Integer> {

  @Mixin protected InputDirectoryParameter inputDirectory;

  @Option(
      names = "--type",
      paramLabel = "<TYPE>",
      type = SmartcardType.class,
      description = "Type of Smartcard to show ${COMPLETION-CANDIDATES} (default=${DEFAULT-VALUE})")
  protected SmartcardType smartcardType = SmartcardType.EGK;

  @Override
  public Integer call() {
    val sca = SmartcardArchive.from(inputDirectory.getInputPath().toFile());
    val constructor = createConstructorForType();
    sca.getICCSNsFor(smartcardType)
        .forEach(
            iccsn -> {
              val smartcard = constructor.apply(sca, iccsn);
              printSmartcardInformation(smartcard);
            });
    return 0;
  }

  private void printSmartcardInformation(Smartcard smartcard) {
    val ownerData = smartcard.getOwnerData();
    System.out.println(format("{0} (ICSSN: {1})", smartcard.getType(), smartcard.getIccsn()));
    System.out.println(
        format(
            "Owner: {0} ({1} {2})",
            ownerData.getCommonName(), ownerData.getSurname(), ownerData.getGivenName()));

    System.out.println(format("\tOrganization:\t{0}", ownerData.getOrganization()));
    System.out.println(format("\tOrg. Units:\t\t{0}", ownerData.getOrganizationUnit()));
    System.out.println(format("\tLocality:\t\t{0}", ownerData.getLocality()));

    System.out.println(
        format(
            "\tAddress:\t\t{0} {1} {2} {3}",
            ownerData.getCountry(),
            ownerData.getPostalCode(),
            ownerData.getLocality(),
            ownerData.getStreet()));

    val authCertificate = smartcard.getAutCertificate();
    System.out.println(
        format("Auth Certificate: {0} {1}", authCertificate.getCryptoSystem(), authCertificate));
    System.out.println(
        format(
            "\tAUT.OIDs:\t{0}",
            smartcard.getAutOids().stream()
                .map(oid -> format("{0} [{1}]", oid.getType(), oid.getId()))
                .collect(Collectors.joining(", "))));
    System.out.println(format("\tPrivateKey:\t{0}", smartcard.getPrivateKeyBase64()));
    System.out.println(
        format(
            "\tPublicKey:\t{0}",
            Base64.getEncoder().encodeToString(smartcard.getAuthPublicKey().getEncoded())));

    System.out.println("--------------");
  }

  private BiFunction<SmartcardArchive, String, Smartcard> createConstructorForType() {
    return switch (this.smartcardType) {
      case EGK -> SmartcardArchive::getEgkByICCSN;
      case SMC_B -> SmartcardArchive::getSmcbByICCSN;
      case HBA -> SmartcardArchive::getHbaByICCSN;
      default -> throw new InvalidSmartcardTypeException(this.smartcardType);
    };
  }
}
