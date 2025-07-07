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

package de.gematik.bbriccs.rest.vzd;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.vzd.util.VzdSummaryPrinter;
import de.gematik.bbriccs.rest.UnirestHttpClient;
import de.gematik.bbriccs.rest.fd.plugins.FixedBearerTokenHeaderProvider;
import de.gematik.bbriccs.rest.idp.IdpTokenHeaderProvider;
import de.gematik.bbriccs.rest.plugins.BasicHttpLogger;
import de.gematik.bbriccs.rest.vzd.search.VzdSearch;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import lombok.val;

// TODO: remove me later!
@SuppressWarnings({"java:S106", "java:S6418"})
public class ExampleMain {

  private static final int MAX_COUNT = 50;

  public static void main(String[] args) {
    val fhirVzdUrl = "https://fhir-directory-ref.vzd.ti-dienste.de/fdv/search/";
    //    val vzdTokenUrl = "https://vzd.erezept-dev.gematik.de/api/vzd/token";
    val dd = "https://idp-test.app.ti-dienste.de/.well-known/openid-configuration";

    val token = System.getenv("VZD_API_TOKEN");

    val sca = SmartcardArchive.fromResources();
    val smcb = sca.getSmcB(0);
    val idpProvider = IdpTokenHeaderProvider.withDiscoveryDocumentUrl(dd);

    val httpClient =
        UnirestHttpClient.forUrl(fhirVzdUrl)
            .register(BasicHttpLogger.toStdout())
            .header(FixedBearerTokenHeaderProvider.withFixedToken(token))
            .withoutTlsVerification();
    val client = VzdClient.withHttpClient(httpClient).withoutFhirValidation();

    //    val searchRequest = new
    // VzdRequestHealthcareServiceSearch("organization.active=true&organization.type=1.2.276.0.76.4.59&_include=HealthcareService%3Aorganization&_count=1&organization.identifier=https%3A%2F%2Fgematik.de%2Ffhir%2Fsid%2Ftelematik-id%7C&organization.identifier=http%3A%2F%2Ffhir.de%2FStructureDefinition%2Fidentifier-iknr%7C104940005");

    val searchRequest =
        VzdSearch.forAnyProfession()
            //            .forPublicPharmacies()
            //            .forProfession(ProfessionOID.BS_KTR)
            //            .forProfession(ProfessionOID.BS_KTR)
            .inCity("Wuppertal")
            //            .nearBy(52.5246997, 13.3904614) // Friedrichstraße
            //            .withTelematikId("8-01-0000000329")
            .withIknr(IKNR.asSidIknr("104940005"))
            .withName("BARMER")
            //            .withMaxCount(100)
            .build();

    val response = client.request(searchRequest);
    val hcs = response.getExpectedResource();

    val serviceTriples = hcs.getHealthcareServiceTriples();

    val printer = new VzdSummaryPrinter();

    serviceTriples.stream()
        //          .filter(VzdHealthServiceTriple::hasAvailableTime)
        .forEach(printer::printSummary);

    System.out.println("Bundle with: " + serviceTriples.size() + " Healthcare services");
  }
}
